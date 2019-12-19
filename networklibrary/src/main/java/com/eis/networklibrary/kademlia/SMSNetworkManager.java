package com.eis.networklibrary.kademlia;

import com.eis.communication.Peer;
import com.eis.communication.network.FindNodeListener;
import com.eis.communication.network.FindValueListener;
import com.eis.communication.network.Invitation;
import com.eis.communication.network.JoinListener;
import com.eis.communication.network.NetworkManager;
import com.eis.communication.network.PingListener;
import com.eis.communication.network.SerializableObject;
import com.eis.smslibrary.SMSHandler;
import com.eis.smslibrary.SMSMessage;
import com.eis.smslibrary.SMSPeer;
import com.eis.smslibrary.listeners.SMSSentListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import static com.eis.networklibrary.kademlia.SMSCommandMapper.SPLIT_CHAR;
import static com.eis.networklibrary.kademlia.SMSDistributedNetworkDictionary.KADEMLIA_K;
import static com.eis.networklibrary.kademlia.SMSDistributedNetworkDictionary.NO_BUCKETS;

/**
 * Singleton class that handles the Kademlia network.
 * If you want to join a network you have to
 * 1. Setup the manager with SMSNetworkManager.getInstance().setup(...)
 * 2. Set a callback listener for events like join proposal with SMSNetworkManager.getInstance().setCallbackListener(...)
 *
 * @author Marco Mariotto
 * @author Alessandra Tonin
 * @author Alberto Ursino
 * @author Luca Crema
 */
@SuppressWarnings("WeakerAccess")
public class SMSNetworkManager implements NetworkManager<SMSKADPeer, SerializableObject, SerializableObject, KADInvitation> {

    enum RequestType {
        JOIN_PROPOSAL,
        PING,
        STORE,
        DELETE,
        FIND_NODES,
        FIND_VALUE
    }

    enum ReplyType {
        JOIN_AGREED,
        PING_ECHO,
        NODES_FOUND,
        VALUE_FOUND,
        VALUE_NOT_FOUND
    }

    protected static SMSNetworkManager instance;
    protected String networkName;
    protected SMSKADPeer mySelf;
    protected SerializableObjectParser valueParser;
    private SMSDistributedNetworkDictionary<SerializableObject> dict;
    private HashMap<KADAddress, ClosestPQ> bestSoFarClosestNodes = new HashMap<>();
    private ArrayList<KADInvitation> invitationList;
    private JoinListener joinListener;

    static final int KADEMLIA_ALPHA = 1; //always less than KADEMLIA_K
    static final int KADEMLIA_REPUBLISH_PERIOD = 60 * 60 * 1000; //1 hour
    static final int KADEMLIA_REFRESH_PERIOD = 60 * 60 * 1000; //1 hour

    //For each bucket, we store the time of the last lookup of any key belonging to this bucket in milliseconds (from the Unix epoch).
    //Every KADEMLIA_REFRESH_PERIOD / 2 milliseconds a RefreshService checks whether the last lookup
    //happened more than KADEMLIA_REFRESH_PERIOD / 2 milliseconds before
    final long[] lastRefresh = new long[NO_BUCKETS];

    private SMSNetworkListenerHandler listenerHandler;

    private SMSNetworkManager() {
        //Private because of singleton
    }

    synchronized public static SMSNetworkManager getInstance() {
        if (instance == null)
            instance = new SMSNetworkManager();
        return instance;
    }

    /**
     * Sets up a new network.
     *
     * @param networkName Name of the network being created
     * @param mySelf      The current peer executing setup()
     */
    public void setup(String networkName, SMSPeer mySelf, SerializableObjectParser valueParser) {
        this.networkName = networkName;
        this.mySelf = new SMSKADPeer(mySelf);
        dict = new SMSDistributedNetworkDictionary<>(new SMSKADPeer(mySelf));
        SMSHandler.getInstance().setReceivedListener(SMSNetworkListener.class);
        this.valueParser = valueParser;
        invitationList = new ArrayList<>();
        listenerHandler = new SMSNetworkListenerHandler();
    }


    /**
     * Sets up refreshing/republishing services. Schedules them to start after the specified delay.
     * Each service does a periodic check.
     */
    private void setupServices(){
        Calendar cal = Calendar.getInstance();
        //next republish starts KADEMLIA_REPUBLISH_PERIOD milliseconds from now
        cal.setTimeInMillis(System.currentTimeMillis() + KADEMLIA_REPUBLISH_PERIOD);
        RepublishService.startTask(cal.getTime());
        //next refresh starts KADEMLIA_REFRESH_PERIOD milliseconds from now
        cal.setTimeInMillis(System.currentTimeMillis() + KADEMLIA_REFRESH_PERIOD);
        RefreshService.startTask(cal.getTime());
    }

    //*******************************************************************************************
    //JOIN

    /**
     * Sends an invitation to the specified peer
     *
     * @param peer who is asked to join the network
     */
    @Override
    synchronized public void invite(final SMSKADPeer peer) {
        SMSCommandMapper.sendRequest(RequestType.JOIN_PROPOSAL, networkName, peer, new SMSSentListener() {
            @Override
            public void onSMSSent(SMSMessage message, SMSMessage.SentState sentState) {
                invitationList.add(new KADInvitation(mySelf, peer, networkName));
            }
        });
    }

    /**
     * Join the network
     *
     * @param invitation The invitation message
     * @author Alessandra Tonin
     */
    @Override
    public void join(KADInvitation invitation) {
        if(invitation.getGuest() != mySelf)
            throw new IllegalArgumentException("The invitation is not valid: it is intended for another user");
        SMSKADPeer inviter = invitation.getInviter();
        dict.addUser(inviter);
        SMSCommandMapper.sendReply(ReplyType.JOIN_AGREED, inviter);
        findClosestNodes(mySelf.networkAddress, new FindNodeListener<SMSKADPeer>() {
            @Override
            public void OnKClosestNodesFound(SMSKADPeer[] peers) {
                //a node lookup for ourselves has been performed
                //note that peers have already been added to the dict
                for(int i = 0; i < NO_BUCKETS; i++)
                    refreshBucket(i);
            }
        });
        setupServices();
    }

    /**
     * Method called when a join proposal from this peer has been accepted
     *
     * @param peer the peer who accepted to join the network
     * @author Alessandra Tonin
     */
    synchronized void onJoinAgreedReply(SMSPeer peer) {
        SMSKADPeer newUser = new SMSKADPeer(peer);
        for(KADInvitation inv : invitationList)
            if(inv.getGuest().equals(newUser)){
                dict.addUser(newUser);
                //Get my resources. If the new node is closer to resource x, we send him a STORE request for x (this is an optimization)
                ArrayList<KADAddress> myResources = dict.getKeys();
                for (int i = 0; i < myResources.size(); i++) {
                    KADAddress resourceKey = myResources.get(i);
                    KADAddress closer = KADAddress.closerToTarget(mySelf.networkAddress, newUser.networkAddress, resourceKey);
                    if (closer.equals(newUser.networkAddress))
                        SMSCommandMapper.sendRequest(RequestType.STORE, resourceKey.toString() + SPLIT_CHAR + dict.getValue(resourceKey).toString(), peer);
                }
                invitationList.remove(inv);
                break;
            }
        //ignore this fake reply
    }

    /**
     * Method called when we receive a join proposal from someone. This calls the listener set up for handling
     * join proposals. If the user accepts the join request, he MUST call join passing the invitation.
     *
     * @param invitation received
     * @author Alessandra Tonin
     */
    synchronized void onJoinProposal(KADInvitation invitation) {
        joinListener.onJoinProposal(invitation);
    }

    /**
     * This method sets a JoinProposalListener
     *
     * @param listener the listener to be set
     */
    synchronized public void setJoinProposalListener(JoinListener listener) {
        joinListener = listener;
    }

    //*******************************************************************************************
    //RESOURCE

    /**
     * Sets a new resource. TODO If {@code key} already exists in the network, this method is unsafe to call and leads to security flaws
     * TODO so we need special permissions in order to change an existing key, for example if we created it
     *
     * @param key   the resource key
     * @param value the resource value
     */
    @Override
    synchronized public void setResource(final SerializableObject key, final SerializableObject value) {
        final KADAddress resKadAddress = new KADAddress(key.toString());
        updateLastLookup(resKadAddress);
        findClosestNodes(resKadAddress, new FindNodeListener<SMSKADPeer>() {
            @Override
            public void OnKClosestNodesFound(SMSKADPeer[] peers) {
                for (SMSKADPeer p : peers)
                    if(p != mySelf)
                        SMSCommandMapper.sendRequest(RequestType.STORE, resKadAddress + SPLIT_CHAR + valueParser.serialize(value), p);
                    else
                        dict.setResource(resKadAddress, value);
            }
        });

    }

    /**
     * Delete an existing resource. TODO If {@code key} has not been created by mySelf, this method is unsafe to call and leads to security flaws
     *
     * @param key The resource key for which to set the value to null
     */
    @Override
    synchronized public void removeResource(final SerializableObject key) {
        final KADAddress resKadAddress = new KADAddress(key.toString());
        updateLastLookup(resKadAddress);
        findClosestNodes(resKadAddress, new FindNodeListener<SMSKADPeer>() {
            @Override
            public void OnKClosestNodesFound(SMSKADPeer[] peers) {
                for (SMSKADPeer p : peers)
                    if(p != mySelf)
                        SMSCommandMapper.sendRequest(RequestType.DELETE, resKadAddress.toString(), p);
                    else
                        dict.removeResource(resKadAddress);
            }
        });
    }

    /**
     * Method called when a STORE request is received.
     *
     * @param requestContent The information about the (key, value) to store, must be parsed.
     */
    synchronized protected void onStoreRequest(String requestContent) {
        String[] splitStr = requestContent.split(SPLIT_CHAR);
        dict.setResource(KADAddress.fromHexString(splitStr[0]), valueParser.deSerialize(splitStr[1]));
    }

    //*******************************************************************************************
    //NODES

    /**
     * Finds k-closest nodes to {@code address}
     *
     * @param address The {@link KADAddress} object for which to find the peer
     * @param listener   Called when the the k-closest nodes are found
     * @throws IllegalStateException if there's already a pending find request for this address
     */
    synchronized private void findClosestNodes(KADAddress address, FindNodeListener<SMSKADPeer> listener) throws IllegalStateException {

        if (listenerHandler.isNodeAddressRegistered(address)) //TODO we need a timer for this
            throw new IllegalStateException("A find request for this key is already pending");
        listenerHandler.registerNodeListener(address, listener); //listener takes care of removing itself from the map when the closest nodes are returned

        //bestSoFarClosestNodes contains the best so far KADEMLIA_K nodes found closer to kadAddress
        ClosestPQ currentBestPQ = new ClosestPQ(new SMSKADPeer.SMSKADComparator(address), dict.getAllUsers());
        bestSoFarClosestNodes.put(address, currentBestPQ);

        //Sends a FIND_CLOSEST_NODES request to first KADEMLIA_ALPHA nodes in closestNodes
        for (int i = 0; i < Math.min(KADEMLIA_ALPHA, currentBestPQ.size()); i++) {
            currentBestPQ.get(i).second = true; //set it to queried
            SMSCommandMapper.sendRequest(RequestType.FIND_NODES, address.toString(), currentBestPQ.get(i).first);
        }
        //since a queried node may return ourselves among its k-closest nodes, we would end up
        //adding ourselves as a non queried node. So to avoid this, we add ourselves to the PQ with a true flag indicating
        //that a query to it isn't necessary
        currentBestPQ.add(mySelf, true);
        updateLastLookup(address);
    }

    /**
     * Method called when a {@link RequestType#FIND_NODES} request is received. Sends a {@link ReplyType#NODES_FOUND} command back.
     *
     * @param sender         who requested the node search
     * @param requestContent contains a kad address that sender wants to know about
     */
    synchronized protected void onFindCloserNodesRequest(SMSPeer sender, String requestContent) {
        dict.addUser(new SMSKADPeer(sender)); //might be a new node we don't know about
        ArrayList<SMSKADPeer> closerNodes = dict.getNodesSortedByDistance(KADAddress.fromHexString(requestContent)); //this includes mySelf
        StringBuilder replyContent = new StringBuilder(requestContent); //this is the address we were asked to look up
        for (int i = 0; i < Math.min(KADEMLIA_K, closerNodes.size()); i++) { //we send up to K closest nodes we know about
            replyContent.append(SPLIT_CHAR);
            replyContent.append(closerNodes.get(i).getAddress()); //phone number
        }
        SMSCommandMapper.sendReply(ReplyType.NODES_FOUND, replyContent.toString(), sender);
    }

    /**
     * Method called when a {@link ReplyType#NODES_FOUND} reply is received
     *
     * @param replyContent a string representing the reply containing the closer nodes according to the node previously contacted
     */
    synchronized protected void onCloserNodesFoundReply(String replyContent) {
        String[] splitStr = replyContent.split(SMSCommandMapper.SPLIT_CHAR);
        KADAddress address = KADAddress.fromHexString(splitStr[0]); //address which we asked to find
        ClosestPQ currentBestPQ = bestSoFarClosestNodes.get(address); //this SHOULD BE ALWAYS NON NULL

        for (int i = 1; i < splitStr.length; i++) { //start from 1 because the first element is address, while the other elements are phone numbers of closer nodes
            SMSKADPeer p = new SMSKADPeer(splitStr[i]);
            currentBestPQ.add(p, false); //if it is already in the queue, this does nothing
            dict.addUser(p); //might be a new node we don't know about, so we add it to our dict
        }

        int picked = 0;

        for (int i = 0; i < currentBestPQ.size() && picked < KADEMLIA_ALPHA; i++) {  //pick other alpha non queried nodes in currentBestPQ
            ClosestPQ.MutablePair<SMSKADPeer, Boolean> pair = currentBestPQ.get(i);
            if (!pair.second) { //if not queried
                picked++;
                pair.second = true;
                SMSCommandMapper.sendRequest(RequestType.FIND_NODES, splitStr[0], pair.first); //splitStr[0] is address
            }
        }

        if (picked == 0) { //our PQ consists only of already queried nodes, which are the closest globally
            bestSoFarClosestNodes.remove(address);
            listenerHandler.triggerKNodesFound(address, currentBestPQ.getAllPeers());
        }
    }

    //*******************************************************************************************
    //VALUE

    /**
     * Method used to find a value of the given key
     *
     * @param key      The resource key of which we want to find the value
     * @param listener The listener that has to be called when the value has been found
     * @throws IllegalStateException if there's already a pending find request fort this address
     */
    synchronized public void findValue(SerializableObject key, FindValueListener listener) throws IllegalStateException {
        KADAddress keyAddress = new KADAddress(key.toString());

        if (listenerHandler.isValueAddressRegistered(keyAddress)) //TODO we need a timer for this
            throw new IllegalStateException("A find request for this key is already pending");
        listenerHandler.registerValueListener(keyAddress, listener);

        //Maybe the value we are looking for is in our local dictionary
        SerializableObject localValue = dict.getValue(keyAddress);
        if (localValue != null)
            listenerHandler.triggerValueFound(keyAddress, localValue);
        else {
            ClosestPQ currentBestPQ = new ClosestPQ(new SMSKADPeer.SMSKADComparator(keyAddress), dict.getAllUsers());
            bestSoFarClosestNodes.put(keyAddress, currentBestPQ);

            for (int i = 0; i < Math.min(KADEMLIA_ALPHA, currentBestPQ.size()); i++) {
                currentBestPQ.get(i).second = true; //set it to queried
                SMSCommandMapper.sendRequest(RequestType.FIND_VALUE, keyAddress.toString(), currentBestPQ.get(i).first);
            }
            //since a queried node may return ourselves among its k-closest nodes, we would end up
            //adding ourselves as a non queried node. So to avoid this, we add ourselves to the PQ with a true flag indicating
            //that a query to it isn't necessary
            currentBestPQ.add(mySelf, true);
        }
        updateLastLookup(keyAddress);
    }

    /**
     *Method called when a {@link RequestType#FIND_VALUE} request is received.
     * Sends a {@link ReplyType#VALUE_FOUND} or {@link ReplyType#VALUE_NOT_FOUND}command back.
     *
     * @param sender         who requested the value
     * @param requestContent contains the key
     */
    synchronized protected void onFindValueRequest(SMSPeer sender, String requestContent) {

        String[] splitStr = requestContent.split(SPLIT_CHAR);
        KADAddress keyAddress = KADAddress.fromHexString(splitStr[0]); //key whose value we are looking for

        //returns the value to the sender if present in the local dict, otherwise returns the k closest nodes (for me) to the sender
        SerializableObject localValue = dict.getValue(keyAddress);
        if (localValue != null)
            SMSCommandMapper.sendReply(ReplyType.VALUE_FOUND, splitStr[0] + SMSCommandMapper.SPLIT_CHAR + localValue.toString(), sender);
        else {
            dict.addUser(new SMSKADPeer(sender));
            ArrayList<SMSKADPeer> closerNodes = dict.getNodesSortedByDistance(keyAddress);
            StringBuilder replyContent = new StringBuilder(requestContent);
            for (int i = 0; i < Math.min(KADEMLIA_K, closerNodes.size()); i++) {
                replyContent.append(SMSCommandMapper.SPLIT_CHAR);
                replyContent.append(closerNodes.get(i).getAddress());
            }
            SMSCommandMapper.sendReply(ReplyType.VALUE_NOT_FOUND, replyContent.toString(), sender);
        }
    }

    /**
     * Method called when a {@link ReplyType#VALUE_NOT_FOUND} reply has been received
     *
     * @param replyContent a string representing the reply, containing closer nodes to the key according to the node we previously contacted
     */
    synchronized public void onValueNotFoundReply(String replyContent) {
        String[] splitStr = replyContent.split(SMSCommandMapper.SPLIT_CHAR);
        KADAddress address = KADAddress.fromHexString(splitStr[0]); //address which we asked to find
        ClosestPQ currentBestPQ = bestSoFarClosestNodes.get(address); //this SHOULD BE ALWAYS NON NULL

        for (int i = 1; i < splitStr.length; i++) {
            SMSKADPeer p = new SMSKADPeer(splitStr[i]);
            currentBestPQ.add(p, false);
            dict.addUser(p);
        }
        int picked = 0;
        for (int i = 0; i < currentBestPQ.size() && picked < KADEMLIA_ALPHA; i++) {
            ClosestPQ.MutablePair<SMSKADPeer, Boolean> pair = currentBestPQ.get(i);
            if (!pair.second) { //if not queried
                picked++;
                pair.second = true;
                SMSCommandMapper.sendRequest(RequestType.FIND_VALUE, splitStr[0], pair.first);
            }
        }

        if (picked == 0) {
            bestSoFarClosestNodes.remove(address);
            //I asked all the k-nodes closest to the resource and none of them has it.
            listenerHandler.triggerValueNotFound(address);
        }
    }

    /**
     * Method called when a {@link ReplyType#VALUE_FOUND} reply is received
     *
     * @param replyContent a string representing the reply, containing the value
     */
    synchronized protected void onValueFoundReply(String replyContent) {
        String[] splitStr = replyContent.split(SPLIT_CHAR);
        KADAddress key = KADAddress.fromHexString(splitStr[0]);
        SerializableObject value = valueParser.deSerialize(splitStr[1]);
        listenerHandler.triggerValueFound(key, value);
    }


    //*******************************************************************************************
    //PING

    /**
     * Method called to ping a node
     *
     * @param peer     the node we want to ping
     * @param listener a {@link PingListener} listener, called when the ping request either times out, or gets a reply
     * @author Alessandra Tonin
     */
    synchronized public void ping(SMSPeer peer, PingListener listener) {
        SMSCommandMapper.sendRequest(RequestType.PING, peer);
        listenerHandler.registerPingListener(peer, listener);
    }

    /**
     *Method called when a {@link RequestType#PING} request has been received. Sends a {@link ReplyType#PING_ECHO) command back.
     *
     * @param peer who requested a ping
     * @author Alessandra Tonin
     */
    synchronized protected void onPingRequest(SMSPeer peer) {
        SMSCommandMapper.sendReply(ReplyType.PING_ECHO, peer);
        dict.addUser(new SMSKADPeer(peer));
    }

    /**
     * Method called when a {@link ReplyType#PING_ECHO) reply is received. We are sure this node is alive
     *
     * @param peer user that replied to the ping
     * @author Alessandra Tonin
     */
    synchronized protected void onPingEchoReply(SMSPeer peer) {
        listenerHandler.triggerPingReply(peer);
    }

    //*******************************************************************************************
    //REFRESH and REPUBLISH

    /**
     * Refreshes the specified bucket. After a join, it is called by the RefreshService only, if needed.
     *
     * @param bucketIndex identifies each bucket, from 0 to N-1, where N = NO_BUCKETS.
     * @author Alessandra Tonin
     */
    void refreshBucket(int bucketIndex) {
        //TODO maybe add a listener so we know when the refresh has completed
        KADAddress randomAddress = dict.getRandomAddressInBucket(bucketIndex);
        findClosestNodes(randomAddress, null); //will trigger the listener handler, but no listener will actually be called
    }

    /**
     * Updates the last lookup of {@code address} to current time.
     *
     * @param address a {@link KADAddress}
     */
    private void updateLastLookup(KADAddress address){
        int index = dict.getBucketContaining(address);
        if(index != -1){
            synchronized (lastRefresh){
                lastRefresh[index] = System.currentTimeMillis();
            }
        }
    }

    /**
     * Republishes all keys of the local dictionary. Called by the RepublishService only every {@link #KADEMLIA_REPUBLISH_PERIOD} milliseconds.
     *
     * @author Alessandra Tonin
     */
    synchronized public void republishKeys() {
        ArrayList<KADAddress> myResources = dict.getKeys();
        for (int i = 0; i < myResources.size(); i++) {
            final KADAddress resourceKey = myResources.get(i);
            FindNodeListener listener = new FindNodeListener() {
                @Override
                public void OnKClosestNodesFound(Peer[] peers) {
                    for (Peer p : peers)
                        SMSCommandMapper.sendRequest(RequestType.STORE, resourceKey.toString() + SPLIT_CHAR + dict.getValue(resourceKey).toString(), (SMSKADPeer) p);
                }
            };
            findClosestNodes(resourceKey, listener);
        }
    }
}
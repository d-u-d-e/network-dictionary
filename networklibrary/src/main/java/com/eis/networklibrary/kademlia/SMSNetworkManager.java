package com.eis.networklibrary.kademlia;

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
public class SMSNetworkManager implements NetworkManager<SMSKADPeer, SerializableObject, SerializableObject> {

    enum RequestType {
        JOIN_PROPOSAL,
        PING,
        STORE,
        DELETE,
        FIND_NODE,
        FIND_VALUE,
        FIND_VALUE_NODE_FOUND
    }

    enum ReplyType {
        JOIN_AGREED,
        PING_ECHO,
        NODE_FOUND,
        NODES_FOR_FIND_VALUE,
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

    private SMSNetworkListenerHandler listenerHandler;

    //*******************************************************************************************

    private SMSNetworkManager() {
        //Private because of singleton
    }

    public static SMSNetworkManager getInstance() {
        if (instance == null)
            instance = new SMSNetworkManager();
        return instance;
    }

    /**
     * Sets up a new network
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
    //*******************************************************************************************

    /**
     * Sends an invitation to the specified peer
     *
     * @param peer The peer of the user to invite
     */
    @Override
    public void invite(final SMSKADPeer peer) {
        SMSCommandMapper.sendRequest(RequestType.JOIN_PROPOSAL, networkName, peer, new SMSSentListener() {
            @Override
            public void onSMSSent(SMSMessage message, SMSMessage.SentState sentState) {
                //listenerHandler.addToInvitedList(peer);
            }
        });
    }

    /**
     * Inserts the {@link SMSMessage} peer in the network
     *
     * @param invitation The invitation message
     */
    public void join(Invitation<SMSKADPeer> invitation) {
        SMSKADPeer inviter = invitation.getInviter();
        dict.addUser(inviter);
        SMSCommandMapper.sendReply(ReplyType.JOIN_AGREED, inviter);
        SMSCommandMapper.sendRequest(RequestType.FIND_NODE, mySelf.getNetworkAddress().toString(), inviter);
        refresh();
    }

    /**
     * Method called when a join proposal this peer has made has been accepted
     *
     * @param peer the peer who accepted to join the network
     */
    void onJoinAgreedReply(SMSPeer peer) {
        SMSKADPeer newUser = new SMSKADPeer(peer);
        dict.addUser(newUser);
        //ricavo le mie risorse e per ognuna di esse controllo se la distanza da me è maggiore di quella dal nuovo peer --> se è maggiore mando a lui la richiesta di STORE
        ArrayList<KADAddress> myResources = dict.getKeys();
        for (int i = 0; i < myResources.size(); i++) {
            KADAddress resourceKey = myResources.get(i);
            KADAddress closer = KADAddress.closerToTarget(mySelf.getNetworkAddress(), newUser.getNetworkAddress(), resourceKey);
            if (closer.equals(newUser.getNetworkAddress())) {
                //TODO: check the content (in particular the value format)
                SMSCommandMapper.sendRequest(RequestType.STORE, resourceKey + SPLIT_CHAR + dict.getValue(resourceKey), peer);
            }
        }
    }

    /**
     * Method called when we receive a join proposal from someone.
     *
     * @param peer           Who invited you to join the network.
     * @param requestContent There should be the name of the network you're invited to
     */
    void onJoinProposal(SMSPeer peer, String requestContent) {
        SMSKADPeer kadPeer = new SMSKADPeer(peer);
        joinListener.onJoinProposal(new KADInvitation(kadPeer, requestContent));
    }

    /**
     * This method sets a JoinProposalListener
     *
     * @param listener the listener to be set
     */
    public void setJoinProposalListener(JoinListener listener) {
        joinListener = listener;
    }
    //*******************************************************************************************

    /**
     * Sets a new resource. TODO If {@code key} already exists in the network, this method is unsafe to call and leads to security flaws
     * TODO so we need special permissions in order to change an existing key, for example if we created it
     *
     * @param key   the resource key
     * @param value the resource value
     */
    @Override
    public void setResource(final SerializableObject key, final SerializableObject value) {
        final KADAddress resKadAddress = new KADAddress(key.toString());
        findNode(resKadAddress, new FindNodeListener<SMSKADPeer>() {
            @Override
            public void OnKClosestNodesFound(SMSKADPeer[] peers) {
                for (SMSKADPeer p : peers)
                    SMSCommandMapper.sendRequest(RequestType.STORE, resKadAddress + SPLIT_CHAR + valueParser.serialize(value), p);
            }
        });
    }

    /**
     * Delete an existing resource. TODO If {@code key} has not been created by mySelf, this method is unsafe to call and leads to security flaws
     *
     * @param key The resource key for which to set the value to null
     */
    @Override
    public void removeResource(SerializableObject key) {
        final KADAddress resKadAddress = new KADAddress(key.toString());
        findNode(resKadAddress, new FindNodeListener<SMSKADPeer>() {
            @Override
            public void OnKClosestNodesFound(SMSKADPeer[] peers) {
                for (SMSKADPeer p : peers)
                    SMSCommandMapper.sendRequest(RequestType.DELETE, resKadAddress.toString(), p);
            }
        });
    }

    /**
     * Method called when a STORE request is received.
     *
     * @param requestContent The information about the (key, value) to store, must be parsed.
     */
    protected void onStoreRequest(String requestContent) {
        String[] splitStr = requestContent.split(SPLIT_CHAR);
        dict.setResource(KADAddress.fromHexString(splitStr[0]), valueParser.deSerialize(splitStr[1]));
    }

    //*******************************************************************************************


    /**
     * Finds k-closest nodes to the one given
     *
     * @param kadAddress The {@link KADAddress} object for which to find the peer
     * @param listener   Called when the the k-closest nodes are found
     * @throws IllegalStateException if there's already a pending find request fort this address
     */
    private void findNode(KADAddress kadAddress, FindNodeListener<SMSKADPeer> listener) throws IllegalStateException {

        if (listenerHandler.isNodeAddressRegistered(kadAddress)) //TODO we need a timer for this
            throw new IllegalStateException("A find request for this key is already pending");
        listenerHandler.registerNodeListener(kadAddress, listener); //listener takes care of removing itself from the map when the closest nodes are returned

        //bestSoFarClosestNodes contains the best so far KADEMLIA_K nodes found closer to kadAddress
        ClosestPQ currentBestPQ = new ClosestPQ(new SMSKADPeer.SMSKADComparator(kadAddress), dict.getAllUsers());
        bestSoFarClosestNodes.put(kadAddress, currentBestPQ);

        //Sends a FIND_NODE request to first KADEMLIA_ALPHA nodes in closestNodes
        for (int i = 0; i < Math.min(KADEMLIA_ALPHA, currentBestPQ.size()); i++) {
            currentBestPQ.get(i).second = true; //set it to queried
            SMSCommandMapper.sendRequest(RequestType.FIND_NODE, kadAddress.toString(), currentBestPQ.get(i).first);
        }
        //since a queried node may return ourselves among its k-closest nodes, we would end up
        //adding ourselves as a non queried node. So to avoid this, we add ourselves to the PQ with a true flag indicating
        //that a query to it isn't necessary
        currentBestPQ.add(mySelf, true);
    }

    /**
     * Method called when a FIND_NODE request is received. Sends a {@link ReplyType#NODE_FOUND} command back.
     *
     * @param sender         who requested the node search
     * @param requestContent contains a kad address that sender wants to know about
     */
    protected void onFindNodeRequest(SMSPeer sender, String requestContent) {
        dict.addUser(new SMSKADPeer(sender)); //might be a new node we don't know about
        ArrayList<SMSKADPeer> closerNodes = dict.getNodesSortedByDistance(KADAddress.fromHexString(requestContent)); //this includes mySelf
        StringBuilder replyContent = new StringBuilder(requestContent); //this is the address we were asked to look up
        for (int i = 0; i < Math.min(KADEMLIA_K, closerNodes.size()); i++) { //we send up to K closest nodes we know about
            replyContent.append(SPLIT_CHAR);
            replyContent.append(closerNodes.get(i).getAddress()); //phone number
        }
        SMSCommandMapper.sendReply(ReplyType.NODE_FOUND, replyContent.toString(), sender);
    }

    /**
     * Method called when a NODE_FOUND reply is received
     *
     * @param replyContent a string representing the NODE_FOUND reply
     * @param flag         Its value is 0 if the reply is sent from {@link #onFindNodeRequest}, 1 if the reply is sent from {@link #onFindValueRequest}
     */
    protected void onNodeFoundReply(String replyContent, int flag) {
        String[] splitStr = replyContent.split(SMSCommandMapper.SPLIT_CHAR);
        KADAddress address = KADAddress.fromHexString(splitStr[0]); //address which we asked to find
        ClosestPQ currentBestPQ = bestSoFarClosestNodes.get(address); //this SHOULD BE ALWAYS NON NULL
        int picked;

        //Reply sent by onFindNodeRequest
        if (flag == SMSNetworkListener.FIND_NODE_FLAG) {
            for (int i = 1; i < splitStr.length; i++) { //start from 1 because the first element is address, while the other elements are phone numbers of closer nodes
                SMSKADPeer p = new SMSKADPeer(splitStr[i]);
                currentBestPQ.add(p, false); //if it is already in the queue, this does nothing
                dict.addUser(p); //might be a new node we don't know about, so we add it to our dict
            }
            picked = SMSNetworkListener.FIND_VALUE_FLAG;
            for (int i = 0; i < currentBestPQ.size() && picked < KADEMLIA_ALPHA; i++) {  //pick other alpha non queried nodes in currentBestPQ
                ClosestPQ.MutablePair<SMSKADPeer, Boolean> pair = currentBestPQ.get(i);
                if (!pair.second) { //if not queried
                    picked++;
                    pair.second = true;
                    SMSCommandMapper.sendRequest(RequestType.FIND_NODE, splitStr[0], pair.first); //splitStr[0] is address
                }
            }

            if (picked == 0) { //our PQ consists only of already queried nodes, which are the closest globally
                bestSoFarClosestNodes.remove(address);
                listenerHandler.triggerKNodesFound(address, currentBestPQ.getAllPeers());
            }
        }

        //Reply sent by onFindValueRequest
        if (flag == 1) {
            for (int i = 1; i < splitStr.length; i++) {
                SMSKADPeer p = new SMSKADPeer(splitStr[i]);
                currentBestPQ.add(p, false);
                dict.addUser(p);
            }
            picked = 0;
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
    }

    //*******************************************************************************************

    /**
     * Republishes a key to be retained in the network
     *
     * @param key The key to be republished
     */
    public void republishKey(SerializableObject key) {
        //TODO
    }

    //*******************************************************************************************

    /**
     * Method used to find a value of the given key
     *
     * @param key      The resource key of which we want to find the value
     * @param listener The listener that has to be called when the value has been found
     * @throws IllegalStateException if there's already a pending find request fort this address
     */
    public void findValue(SerializableObject key, FindValueListener listener) throws IllegalStateException {

        KADAddress keyAddress = new KADAddress(key.toString());

        if (listenerHandler.isValueAddressRegistered(keyAddress)) //TODO timer
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
    }

    /**
     * Method called when a FIND_VALUE request is received. Sends a {@link ReplyType#VALUE_FOUND} command back.
     *
     * @param sender         who requested the value
     * @param requestContent contains the key
     */
    protected void onFindValueRequest(SMSPeer sender, String requestContent) {

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
            SMSCommandMapper.sendReply(ReplyType.NODES_FOR_FIND_VALUE, replyContent.toString(), sender);
        }
    }

    /**
     * Method called when a VALUE_FOUND reply is received
     *
     * @param replyContent a string representing the VALUE_FOUND reply
     */
    protected void onValueFoundReply(String replyContent) {
        String[] splitStr = replyContent.split(SPLIT_CHAR);
        KADAddress key = KADAddress.fromHexString(splitStr[0]);
        SerializableObject value = valueParser.deSerialize(splitStr[1]);
        listenerHandler.triggerValueFound(key, value);
    }

    /**
     * Method called when a VALUE_NOT_FOUND reply is received
     *
     * @param replyContent a string representing the VALUE_NOT_FOUND reply
     */
    protected void onValueNotFoundReply(String replyContent) {
        KADAddress key = KADAddress.fromHexString(replyContent);
        listenerHandler.triggerValueNotFound(key);
    }

    //*******************************************************************************************

    /**
     * Method called to PING a node
     *
     * @param peer     the node we want to ping
     * @param listener a {@link PingListener} listener
     */
    public void ping(SMSPeer peer, PingListener listener) {
        SMSCommandMapper.sendRequest(RequestType.PING, peer);
        listenerHandler.registerPingListener(peer, listener);
    }

    /**
     * Method called when a PING request has been received. Sends a {@link ReplyType#PING_ECHO) command back.
     *
     * @param peer who requested a ping
     */
    protected void onPingRequest(SMSPeer peer) {
        SMSCommandMapper.sendReply(ReplyType.PING_ECHO, peer);
        dict.addUser(new SMSKADPeer(peer));
    }

    /**
     * Method called when a PING_ECHO reply is received. We are sure this node is alive.
     *
     * @param peer user that replied to the ping
     */
    protected void onPingEchoReply(SMSPeer peer) {
        listenerHandler.triggerPingReply(peer);
    }


    //*******************************************************************************************

    /**
     * Refresh the buckets of a node (This method must be called when a new node joins the network and also every hour by every node)
     */
    public void refresh() {
        for (int i = 0; i < NO_BUCKETS; i++) {
            KADAddress randomAddress = dict.getRandomAddressInBucket(i);
            //TODO
        }
    }

}
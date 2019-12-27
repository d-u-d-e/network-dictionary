package com.eis.networklibrary.kademlia;

import androidx.annotation.NonNull;

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
import java.util.HashMap;

import static com.eis.networklibrary.kademlia.SMSCommandMapper.SPLIT_CHAR;
import static com.eis.networklibrary.kademlia.SMSDistributedNetworkDictionary.KADEMLIA_K;
import static com.eis.networklibrary.kademlia.SMSDistributedNetworkDictionary.NO_BUCKETS;

/**
 * Singleton class that handles the Kademlia network.
 * If you want to join a network you have to:
 * 1. Setup the manager with SMSNetworkManager.getInstance().setup(...)
 * 2. Set a callback listener for join proposals with SMSNetworkManager.getInstance().setJoinProposalListener(JoinListener listener)
 *
 * @author Marco Mariotto
 * @author Alessandra Tonin
 * @author Alberto Ursino
 * @author Luca Crema
 */
@SuppressWarnings("WeakerAccess")
public class SMSNetworkManager implements NetworkManager<SMSKADPeer, SerializableObject, SerializableObject, KADInvitation> {



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
    }


    /**
     * Sets up refreshing/republishing services. Schedules them to start after the specified delay.
     * Each service does a periodic check. Check out each class for more details.
     * @author Marco Mariotto
     */
    private void setupServices() {
        Calendar cal = Calendar.getInstance();
        //next republish starts KADEMLIA_REPUBLISH_PERIOD milliseconds from now
        cal.setTimeInMillis(System.currentTimeMillis() + KADEMLIA_REPUBLISH_PERIOD_MILLIS);
        RepublishService.startTask(cal.getTime());
        //next refresh starts KADEMLIA_REFRESH_PERIOD milliseconds from now
        cal.setTimeInMillis(System.currentTimeMillis() + KADEMLIA_REFRESH_PERIOD_MILLIS);
        RefreshService.startTask(cal.getTime());
    }



    /**
     * Sends an invitation to the specified peer
     *
     * @param peer who is asked to join the network
     * @author Alberto Ursino, Marco Mariotto
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
     * @author Marco Mariotto
     */
    @Override
    synchronized public void setResource(final SerializableObject key, final SerializableObject value) {
        final KADAddress resKadAddress = new KADAddress(key.toString());
        updateLastLookup(resKadAddress);
        findClosestNodes(resKadAddress, new FindNodeListener<SMSKADPeer>() {
            @Override
            public void OnKClosestNodesFound(SMSKADPeer[] peers) {
                for (SMSKADPeer p : peers)
                    if (p != mySelf)
                        SMSCommandMapper.sendRequest(RequestType.STORE, resKadAddress + SPLIT_CHAR + valueParser.serialize(value), p);
                    else
                        dict.setResource(resKadAddress, value);
            }
        }, 0);

    }

    /**
     * Delete an existing resource. TODO If {@code key} has not been created by mySelf, this method is unsafe to call and leads to security flaws
     *
     * @param key The resource key for which to set the value to null
     * @author Marco Mariotto
     */
    @Override
    synchronized public void removeResource(final SerializableObject key) {
        final KADAddress resKadAddress = new KADAddress(key.toString());
        updateLastLookup(resKadAddress);
        findClosestNodes(resKadAddress, new FindNodeListener<SMSKADPeer>() {
            @Override
            public void OnKClosestNodesFound(SMSKADPeer[] peers) {
                for (SMSKADPeer p : peers)
                    if (p != mySelf)
                        SMSCommandMapper.sendRequest(RequestType.DELETE, resKadAddress.toString(), p);
                    else
                        dict.removeResource(resKadAddress);
            }
        }, 0);
    }

    /**
     * Method called when a STORE request is received.
     *
     * @param requestContent The information about the (key, value) to store, must be parsed.
     * @author Marco Mariotto
     */
    synchronized protected void onStoreRequest(String requestContent) {
        String[] splitStr = requestContent.split(SPLIT_CHAR);
        dict.setResource(KADAddress.fromHexString(splitStr[0]), valueParser.deSerialize(splitStr[1]));
    }

    //*******************************************************************************************
    //NODES

    /**
     * Finds the k-closest nodes to {@code address}
     *
     * @param address  a {@link KADAddress}
     * @param listener called when the the k-closest nodes are found
     * @param maxWaiting Maximum milliseconds to wait before considering this request unsuccessful. If maxWaiting is 0, no time limit is set.
     * @throws IllegalStateException if there's already a pending find request for this address
     * @author Marco Mariotto
     */
    synchronized private void findClosestNodes(KADAddress address, FindNodeListener<SMSKADPeer> listener, int maxWaiting) throws IllegalStateException {

        if (listenerHandler.isNodeAddressRegistered(address) || listenerHandler.isValueAddressRegistered(address))
            throw new IllegalStateException("A request for this address is already pending");
        listenerHandler.registerNodeListener(address, listener, maxWaiting); //listener takes care of removing itself from the register when the closest nodes are returned

        //bestSoFarClosestNodes contains the best so far KADEMLIA_K nodes found closer to address
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
     * @author Marco Mariotto
     */
    synchronized protected void onFindCloserNodesRequest(SMSPeer sender, String requestContent) {
        //We add the sender to our local dictionary, might be a new node we don't know about
        dict.addUser(new SMSKADPeer(sender));
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
     * @author Marco Mariotto
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
            MutablePair<SMSKADPeer, Boolean> pair = currentBestPQ.get(i);
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
     * @param maxWaiting Maximum milliseconds to wait before considering this request unsuccessful. If maxWaiting is 0, no time limit is set.
     * @throws IllegalStateException if there's already a pending find request fort this address
     * @author Alberto Ursino, inspired by Marco Mariotto's code for consistency reasons
     */
    synchronized public void findValue(SerializableObject key, FindValueListener listener, int maxWaiting) throws IllegalStateException {
        KADAddress keyAddress = new KADAddress(key.toString());
        if (listenerHandler.isValueAddressRegistered(keyAddress) || listenerHandler.isNodeAddressRegistered(keyAddress))
            throw new IllegalStateException("A request for this address is already pending");
        listenerHandler.registerValueListener(keyAddress, listener, maxWaiting);

        //Maybe the value we are looking for is in our local dictionary
        SerializableObject localValue = dict.getValue(keyAddress);
        if (localValue != null)
            listenerHandler.triggerValueFound(keyAddress, localValue);
        else {
            ClosestPQ currentBestPQ = new ClosestPQ(new SMSKADPeer.SMSKADComparator(keyAddress), dict.getAllUsers());
            bestSoFarClosestNodes.put(keyAddress, currentBestPQ);

            for (int i = 0; i < Math.min(KADEMLIA_ALPHA, currentBestPQ.size()); i++) {
                currentBestPQ.get(i).second = true; //Sets it to queried
                SMSCommandMapper.sendRequest(RequestType.FIND_VALUE, keyAddress.toString(), currentBestPQ.get(i).first);
            }
            //We have already been queried by checking if we have the resource
            currentBestPQ.add(mySelf, true);
        }
        updateLastLookup(keyAddress);
    }

    /**
     * Method called when a {@link RequestType#FIND_VALUE} request is received.
     * Sends a {@link ReplyType#VALUE_FOUND} or {@link ReplyType#VALUE_NOT_FOUND} command back.
     *
     * @param sender         The user who requested the value
     * @param requestContent String that contains the key
     * @author Alberto Ursino, inspired by Marco Mariotto's code for consistency reasons
     */
    synchronized protected void onFindValueRequest(SMSPeer sender, String requestContent) {
        //We add the sender to our local dictionary, might be a new node we don't know about
        dict.addUser(new SMSKADPeer(sender));

        String[] splitStr = requestContent.split(SPLIT_CHAR);
        String key = splitStr[0];
        KADAddress keyAddress = KADAddress.fromHexString(key);

        //Returns to the sender the value, if present in the local dict, otherwise returns the k closest nodes (for me)
        SerializableObject localValue = dict.getValue(keyAddress);
        if (localValue != null)
            SMSCommandMapper.sendReply(ReplyType.VALUE_FOUND, key + SMSCommandMapper.SPLIT_CHAR + localValue.toString(), sender);
        else {
            ArrayList<SMSKADPeer> closerNodes = dict.getNodesSortedByDistance(keyAddress);
            StringBuilder replyContent = new StringBuilder(requestContent);
            for (int i = 0; i < Math.min(KADEMLIA_K, closerNodes.size()); i++) {
                replyContent.append(SMSCommandMapper.SPLIT_CHAR);
                replyContent.append(closerNodes.get(i).getAddress());
            }
            //In the replyContent variable there are all the k-closest nodes (for me) to the value
            SMSCommandMapper.sendReply(ReplyType.VALUE_NOT_FOUND, replyContent.toString(), sender);
        }
    }

    /**
     * Method called when a {@link ReplyType#VALUE_NOT_FOUND} reply has been received
     *
     * @param replyContent The string representing the reply, containing closer nodes to the key according to the node we previously contacted
     * @author Alberto Ursino, inspired by Marco Mariotto's code for consistency reasons
     */
    synchronized public void onValueNotFoundReply(String replyContent) {
        String[] splitStr = replyContent.split(SMSCommandMapper.SPLIT_CHAR);
        String key = splitStr[0];
        KADAddress keyAddress = KADAddress.fromHexString(key);
        ClosestPQ currentBestPQ = bestSoFarClosestNodes.get(keyAddress); //this SHOULD BE ALWAYS NON NULL

        //Updates my list of k-nodes closest to the resource with those just received in the replyContent
        for (int i = 1; i < splitStr.length; i++) {
            SMSKADPeer p = new SMSKADPeer(splitStr[i]);
            currentBestPQ.add(p, false);
            dict.addUser(p);
        }

        //Flag that updates us on how many FIND_VALUE requests we are sending
        int picked = 0;

        for (int i = 0; i < currentBestPQ.size() && picked < KADEMLIA_ALPHA; i++) {
            MutablePair<SMSKADPeer, Boolean> pair = currentBestPQ.get(i);
            if (!pair.second) { //If the current has not been queried then sends to it a FIND_VALUE request
                picked++;
                pair.second = true;
                SMSCommandMapper.sendRequest(RequestType.FIND_VALUE, key, pair.first);
            }
        }

        if (picked == 0) {
            bestSoFarClosestNodes.remove(keyAddress);
            //I asked all the k-nodes closest to the resource and none of them has it.
            listenerHandler.triggerValueNotFound(keyAddress);
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






    /**
     * Updates the last lookup of {@code address} to current time.
     *
     * @param address a {@link KADAddress}
     * @author Marco Mariotto
     */
    private void updateLastLookup(KADAddress address) {
        int index = dict.getBucketContaining(address);
        if (index != -1) {
            synchronized (lastRefresh) {
                lastRefresh[index] = System.currentTimeMillis();
            }
        }
    }

    
}
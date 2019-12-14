package com.eis.networklibrary.kademlia;

import com.eis.communication.network.NetworkManager;
import com.eis.communication.network.SerializableObject;
import com.eis.smslibrary.SMSHandler;
import com.eis.smslibrary.SMSMessage;
import com.eis.smslibrary.SMSPeer;
import com.eis.smslibrary.listeners.SMSSentListener;

import java.util.ArrayList;
import java.util.HashMap;

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

    protected static SMSNetworkManager instance;
    protected String networkName;
    protected SMSKADPeer mySelf;
    protected SerializableObjectParser keyParser;
    protected SerializableObjectParser valueParser;
    private SMSDistributedNetworkDictionary<SerializableObject> dict;
    //joinSent keeps track of JOIN_PROPOSAL requests still pending.
    private ArrayList<SMSPeer> joinSent = new ArrayList<>();

    static final int ALPHA = 1;

    private HashMap<KADAddress, FindNodeListener> findNodeListenerMap = new HashMap<>();
    private HashMap<KADAddress, FindValueListener> findValueListenerMap = new HashMap<>();

    private SMSNetworkManager() {
        //Private because of singleton
    }

    protected static SMSNetworkManager getInstance() {
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
    public void setup(String networkName, SMSPeer mySelf, SerializableObjectParser keyParser, SerializableObjectParser valueParser) {
        this.networkName = networkName;
        this.mySelf = new SMSKADPeer(mySelf);
        dict = new SMSDistributedNetworkDictionary<>(new SMSKADPeer(mySelf));
        SMSHandler.getInstance().setReceivedListener(SMSNetworkListener.class);
        this.keyParser = keyParser;
        this.valueParser = valueParser;
    }

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
                joinSent.add(peer);
            }
        });
    }

    /**
     * Inserts the {@link SMSMessage} peer in the network
     *
     * @param invitation The invitation message
     */
    public void join(SMSMessage invitation) {
        //TODO: 1. Aggiungere chi ci ha invitato al nostro dizionario
        dict.addUser(new SMSKADPeer(invitation.getPeer()));
        //TODO: 2. Dirgli che ci vogliamo aggiungere alla rete: ovvero inviare un JOIN_AGREED

        //TODO: 3. farci conoscere e ricevere la lista di peer da chi ci ha invitato
    }

    /**
     * Finds the closest node to the given {@param key} and sends to it the STORE request
     *
     * @param key   The resource key to store
     * @param value The resource value to store
     */
    @Override
    public void setResource(final SerializableObject key, final SerializableObject value) {

        KADAddress resKadAddress = new KADAddress(keyParser.serialize(key));
        findNode(resKadAddress, new FindNodeListener() {
            @Override
            public void onClosestNodeFound(SMSKADPeer peer) {
                SMSCommandMapper.sendRequest(RequestType.STORE, keyParser.serialize(key) + "-" + valueParser.serialize(value), peer);
            }
        });

    }

    /**
     * Calls the setResource method passing to it the {@param key} and a null value
     *
     * @param key The resource key for which to set the value to null
     */
    @Override
    public void removeResource(SerializableObject key) {
        setResource(key, null);
    }

    /**
     * Finds the peer of the given KADAddress object
     * If the given KADAddress doesn't exist then finds the peer of the closest (to the one given)
     *
     * @param kadAddress The {@link KADAddress} object for which to find the peer
     * @param listener   Called when the
     * @throws IllegalStateException if there's already a pending find request fort this address
     */
    private void findNode(KADAddress kadAddress, FindNodeListener listener) {

        if (findNodeListenerMap.containsKey(kadAddress))
            throw new IllegalStateException("A find request for this key is already pending");
        findNodeListenerMap.put(kadAddress, listener); //listener should remove itself from this map; maybe there's a better way to achieve this

        //Checks if we are finding ourselves
        if (kadAddress.equals(mySelf.getNetworkAddress()))
            onNodeFoundReply(kadAddress, mySelf, mySelf);

        //Checks if we already know the kadAddress
        SMSKADPeer nodeFoundInLocalDict = dict.getPeerFromAddress(kadAddress);
        if (nodeFoundInLocalDict != null)
            onNodeFoundReply(kadAddress, nodeFoundInLocalDict, mySelf);

        //Creates an ArrayList with the known closest nodes
        ArrayList<SMSKADPeer> knownCloserNodes = dict.getNodesSortedByDistance(kadAddress);

        //Sends a FIND_NODE request to the peers in the ArrayList
        for (int i = 0; i < ALPHA && i < knownCloserNodes.size(); i++)
            SMSCommandMapper.sendRequest(RequestType.FIND_NODE, kadAddress.toString(), knownCloserNodes.get(i));
    }

    /**
     * Republishes a key to be retained in the network
     *
     * @param key The key to be republished
     */
    public void republishKey(SerializableObject key) {

    }

    /**
     * Method used to find a value of the given key
     *
     * @param key      The resource key of which we want to find the value
     * @param listener TODO
     */
    public void findValue(SerializableObject key, FindValueListener listener) {
        KADAddress keyAddress = new KADAddress(keyParser.serialize(key));
        SerializableObject value = dict.getValue(keyAddress);
        if (value != null) {
            listener.onValueFound(value);
            return;
        }
        findValueListenerMap.put(keyAddress, listener);

        //TODO this is similar to findNode, except that when the value is found it is immediately returned

    }

    /**
     * Method called when a PING request has been received. Sends a {@link ReplyType#PING_ECHO) command back.
     *
     * @param peer who requested a ping
     */
    protected void onPingRequest(SMSPeer peer) {
        //TODO should add a randomID to mach ping requests: take a look at sms formats requests
        SMSCommandMapper.sendReply(ReplyType.PING_ECHO, "", peer);
    }

    /**
     * Method called when a join proposal has been accepted
     *
     * @param peer the peer who accepted to join the network TODO: is this the node who accepted or those who invited?
     */
    void onJoinAgreedReply(SMSPeer peer) {

    }

    /**
     * Method called when a FIND_NODE request is received. Sends a {@link ReplyType#NODE_FOUND} command back.
     *
     * @param sender         who requested the node search
     * @param requestContent contains the address of the node sender wants to know about
     */
    protected void onFindNodeRequest(SMSPeer sender, String requestContent) {
        ArrayList<SMSKADPeer> closerNodes = dict.getNodesSortedByDistance(new KADAddress(requestContent));
        //TODO K != 1
        SMSKADPeer closerNode = closerNodes.get(0);
        SMSCommandMapper.sendReply(ReplyType.NODE_FOUND, closerNode.getAddress(), sender);
    }

    /**
     * Method called when a FIND_VALUE request is received. Sends a {@link ReplyType#VALUE_FOUND} command back.
     *
     * @param sender         who requested the value
     * @param requestContent contains the key
     */
    protected void onFindValueRequest(SMSPeer sender, String requestContent) {

    }

    /**
     * Method called when a STORE request is received.
     *
     * @param requestContent The information about the (key, value) to store, must be parsed.
     */
    protected void onStoreRequest(String requestContent) {
        //i am the closest node to the key specified
        String[] splitStr = requestContent.split(SMSCommandMapper.SPLIT_CHAR);
        SerializableObject key = keyParser.deSerialize(splitStr[0]); //should key.toString() be equal to splitStr[0]? In that case this can be simplified
        SerializableObject value = valueParser.deSerialize(splitStr[1]);
        dict.setResource(new KADAddress(key.toString()), value);
    }

    /**
     * Method called when a PING_ECHO reply is received. We are sure this node is alive.
     *
     * @param peer user that replied to the ping.
     */
    protected void onPingEchoReply(SMSPeer peer) {
        //Method called when someone you pinged gives you an answer

    }

    /**
     * Method called when a NODE_FOUND reply is received
     *
     * @param address    the node we are finding for
     * @param closerNode the closer node returned
     * @param sender     the node who informed us back about closerNode
     */
    protected void onNodeFoundReply(KADAddress address, SMSKADPeer closerNode, SMSKADPeer sender) { //TODO k > 1
        //TODO not sure if this if statement is correct: is it guaranteed that a node knowing to be the closer among its buckets nodes is the closest globally?
        if (closerNode.equals(sender))
            findNodeListenerMap.get(address).onClosestNodeFound(closerNode);
        else
            SMSCommandMapper.sendRequest(RequestType.FIND_NODE, address.toString(), closerNode);
    }

    /**
     * Method called when a VALUE_FOUND reply is received
     *
     * @param key   the key of the value we are searching for
     * @param value TODO
     */
    protected void onValueFoundReply(KADAddress key, SerializableObject value) {
        FindValueListener listener = findValueListenerMap.remove(key);
        listener.onValueFound(value);
    }

    enum RequestType {
        JOIN_PROPOSAL,
        PING,
        STORE,
        FIND_NODE,
        FIND_VALUE
    }

    enum ReplyType {
        JOIN_AGREED,
        PING_ECHO,
        NODE_FOUND,
        VALUE_FOUND
    }
}
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

    private SMSNetworkListenerHandler listenerHandler;
    private HashMap<SMSPeer, PingListener> pingListenerMap = new HashMap<>();
    private JoinListener joinListener;

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
    public void setup(String networkName, SMSPeer mySelf, SerializableObjectParser valueParser) {
        this.networkName = networkName;
        this.mySelf = new SMSKADPeer(mySelf);
        dict = new SMSDistributedNetworkDictionary<>(new SMSKADPeer(mySelf));
        SMSHandler.getInstance().setReceivedListener(SMSNetworkListener.class);
        this.valueParser = valueParser;
        listenerHandler = new SMSNetworkListenerHandler();
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
        final KADAddress resKadAddress = new KADAddress(key.toString());
        findNode(resKadAddress, new FindNodeListener() {
            @Override
            public void onClosestNodeFound(SMSKADPeer peer) {
                SMSCommandMapper.sendRequest(RequestType.STORE, resKadAddress + SMSCommandMapper.SPLIT_CHAR + valueParser.serialize(value), peer);
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
        //setResource(key, null); TODO this crashes
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

        if (listenerHandler.isNodeAddressRegistered(kadAddress))
            throw new IllegalStateException("A find request for this key is already pending");
        listenerHandler.registerNodeListener(kadAddress, listener); //listener should remove itself from this map; maybe there's a better way to achieve this

        //Checks if we are finding ourselves
        if (kadAddress.equals(mySelf.getNetworkAddress()))
            listener.onClosestNodeFound(mySelf);

        //Checks if we already know the kadAddress
        SMSKADPeer nodeFoundInLocalDict = dict.getPeerFromAddress(kadAddress);
        if (nodeFoundInLocalDict != null)
            listener.onClosestNodeFound(nodeFoundInLocalDict);

        findNodeListenerMap.put(kadAddress, listener); //listener should remove itself from this map; maybe there's a better way to achieve this

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
        //TODO
    }

    /**
     * Method used to find a value of the given key
     *
     * @param key      The resource key of which we want to find the value
     * @param listener TODO
     */
    public void findValue(SerializableObject key, FindValueListener listener) {
        KADAddress keyAddress = new KADAddress(key.toString());
        SerializableObject value = dict.getValue(keyAddress);
        if (value != null) {
            listenerHandler.triggerValueListener(keyAddress, value);
            return;
        }
        listenerHandler.registerValueListener(keyAddress, listener);

        //TODO this is similar to findNode, except that when the value is found it is immediately returned

    }

    /**
     * Method called to PING a node
     *
     * @param peer     the node we want to ping
     * @param listener a {@link PingListener} listener
     */
    public void ping(SMSPeer peer, PingListener listener) {
        SMSCommandMapper.sendRequest(RequestType.PING, peer);
        pingListenerMap.put(peer, listener);
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
        PingListener listener = pingListenerMap.remove(peer);
        listener.onPingReply(peer);
    }

    /**
     * Method called when a join proposal has been accepted
     *
     * @param peer the peer who accepted to join the network
     */
    void onJoinAgreedReply(SMSPeer peer) {
        //TODO
    }

    void onJoinProposal(SMSPeer sender) {
        joinListener.onJoinProposal(sender);
    }

    public void setJoinProposalListener(JoinListener listener) {
        joinListener = listener;
    }

    /**
     * Method called when a FIND_NODE request is received. Sends a {@link ReplyType#NODE_FOUND} command back.
     *
     * @param sender         who requested the node search
     * @param requestContent contains a kad address that sender wants to know about
     */
    protected void onFindNodeRequest(SMSPeer sender, String requestContent) {
        ArrayList<SMSKADPeer> closerNodes = dict.getNodesSortedByDistance(KADAddress.fromHexString(requestContent));
        //TODO K != 1
        SMSKADPeer closerNode = closerNodes.get(0);
        SMSCommandMapper.sendReply(ReplyType.NODE_FOUND, requestContent + SMSCommandMapper.SPLIT_CHAR + closerNode.getAddress(), sender);
    }

    /**
     * Method called when a FIND_VALUE request is received. Sends a {@link ReplyType#VALUE_FOUND} command back.
     *
     * @param sender         who requested the value
     * @param requestContent contains the key
     */
    protected void onFindValueRequest(SMSPeer sender, String requestContent) {
        //TODO
    }

    /**
     * Method called when a STORE request is received.
     *
     * @param requestContent The information about the (key, value) to store, must be parsed.
     */
    protected void onStoreRequest(String requestContent) {
        String[] splitStr = requestContent.split(SMSCommandMapper.SPLIT_CHAR);
        dict.setResource(KADAddress.fromHexString(splitStr[0]), valueParser.deSerialize(splitStr[1]));
    }


    /**
     * Method called when a NODE_FOUND reply is received
     *
     * @param replyContent a string representing the NODE_FOUND reply
     * @param sender       the node who informed us back about closerNode
     */
    protected void onNodeFoundReply(String replyContent, SMSKADPeer sender) {
        //TODO k > 1
        //TODO not sure if this if statement is correct: is it guaranteed that a node knowing to be the closer among its buckets nodes is the closest globally?

        String[] splitStr = replyContent.split(SMSCommandMapper.SPLIT_CHAR);
        KADAddress address = KADAddress.fromHexString(splitStr[0]);
        SMSKADPeer closerNode = new SMSKADPeer(splitStr[1]); //build a kad address from phone number

        if (closerNode.equals(sender))
            listenerHandler.triggerNodeListener(address, closerNode);
        else
            SMSCommandMapper.sendRequest(RequestType.FIND_NODE, address.toString(), closerNode);
    }

    /**
     * Method called when a VALUE_FOUND reply is received
     *
     * @param replyContent a string representing the VALUE_FOUND reply
     */
    protected void onValueFoundReply(String replyContent) {
        String[] splitStr = replyContent.split(SMSCommandMapper.SPLIT_CHAR);
        KADAddress key = KADAddress.fromHexString(splitStr[0]);
        SerializableObject value = valueParser.deSerialize(splitStr[1]);
        listenerHandler.triggerValueListener(key, value);
    }

    /**
     * Method called when a VALUE_NOT_FOUND reply is received
     *
     * @param replyContent a string representing the VALUE_NOT_FOUND reply
     */
    protected void onValueNotFoundReply(String replyContent) {
        KADAddress key = KADAddress.fromHexString(replyContent);
        FindValueListener listener = findValueListenerMap.remove(key);
        listener.onValueNotFound();
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
        VALUE_FOUND,
        VALUE_NOT_FOUND
    }
}
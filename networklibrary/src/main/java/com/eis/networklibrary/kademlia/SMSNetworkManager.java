package com.eis.networklibrary.kademlia;

import android.os.CountDownTimer;

import com.eis.communication.network.NetworkManager;
import com.eis.communication.network.SerializableObject;
import com.eis.smslibrary.SMSHandler;
import com.eis.smslibrary.SMSMessage;
import com.eis.smslibrary.SMSPeer;
import com.eis.smslibrary.listeners.SMSSentListener;

import java.util.ArrayList;

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
    protected ArrayList<CountDownTimer> pendingTimers;

    final static int TIMER = 60000; //timer is 1 minute, in millis
    final static int INTERVAL = 1000; //timer ticks every second

    static final int ALPHA = 1;

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
     * Sets a (key, value) resource in the local dictionary: this is called only if a STORE message is received
     *
     * @param key   The resource key
     * @param value The resource value
     */
    @Override
    public void setResource(SerializableObject key, SerializableObject value) {
        //TODO Find the closest node and tell to it to store the (key, value) pair
        //TODO 1. Trovare il bucket in cui si trova la risorsa o, se non esiste, quello più vicino ad essa
        //TODO 2. Confrontare gli indirizzi degli utenti di quel bucket con quello della risorsa e prendere il più vicino
        //TODO 3. Rendere responsabile della risorsa l'utente trovato al punto 2

        KADAddress resKadAddress = new KADAddress(key.toString());


    }

    /**
     * Removes a key-value resource from the local dictionary: this is called if a STORE (key, NULL) message is received
     *
     * @param key The resource key
     */
    @Override
    public void removeResource(SerializableObject key) {
        //TODO this call crashes because value is referenced (value.toString()) in setResource(key , value)
        //TODO we should break setResource in two parts: 1) we first find the closest node to a key using findNode, 2) and then we send the store request
        //TODO so we can then use 1) to find the owner of key, and then simply call something like sendRequest(STORE, key.toString()  + "_" + "NULL", closestPeer)
        //setResource(key, null);

    }

    /**
     * Finds the peer of the given KADAddress object
     * If the given KADAddress doesn't exist then finds then peer of the closest (to the one given)
     *
     * @param kadAddress The KADAddress for which to find the peer
     * @return an {@link KADAddress} object
     */
    private void findNode(KADAddress kadAddress) {

        //TODO 4. Procedere in modo ricorsivo sino a quando ricevo il numero di telefono del peer più vicino in assoluto a kadAddress

        if(kadAddress.equals(mySelf.getNetworkAddress())) onNodeFoundReply(mySelf);

        //check if we already know locally kadAddress
        SMSKADPeer nodeFoundInLocalDict = dict.getPeerFromAddress(kadAddress);
        if(nodeFoundInLocalDict != null) onNodeFoundReply(nodeFoundInLocalDict);

        //otherwise
        ArrayList<SMSKADPeer> knownCloserNodes = dict.getNodesSortedByDistance(kadAddress);
        if(knownCloserNodes.get(0).equals(mySelf)) onNodeFoundReply(mySelf);

        for(int i = 0; i < ALPHA && i < knownCloserNodes.size(); i++){
            SMSKADPeer peer = knownCloserNodes.get(i);
            SMSCommandMapper.sendRequest(RequestType.FIND_NODE, kadAddress.toString(), peer);
            final CountDownTimer timer = new CountDownTimer(TIMER, INTERVAL) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    //pendingTimers.remove(timer); non so perchè ma dà errore sulla variabile timer
                }
            };
            timer.start();
            pendingTimers.add(timer);
        }
        //la gestione della risposta va fatta col listener --> quando la ricevo devo chiamare timer.cancel()


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
     * @param resourceKey The resource key of which we want to find the value
     */
    @Override
    public void findValue(SerializableObject resourceKey, ConverseListener listener) {
        KADAddress key = new KADAddress(resourceKey.toString());
        SerializableObject value = dict.getValue(key);
        if (value != null) {
            listener.onValueFound(value);
            return;
        }

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

    void onJoinAgreedReply(SMSPeer peer){

    }

    /**
     * Method called when a FIND_NODE request is received. Sends a {@link ReplyType#NODE_FOUND} command back.
     *
     * @param peer           who requested the node search
     * @param requestContent information about the node to find, must be parsed first
     */
    protected void onFindNodeRequest(SMSPeer peer, String requestContent) {
        KADAddress nodeToSearch = new KADAddress(requestContent);
        ArrayList<SMSKADPeer> closerNodes = dict.getNodesSortedByDistance(nodeToSearch);
        //TODO K != 1
        SMSKADPeer closerNode = closerNodes.get(0);
        SMSCommandMapper.sendReply(ReplyType.NODE_FOUND, closerNode.getAddress(), peer);
    }

    /**
     * Method called when a FIND_VALUE request is received. Sends a {@link ReplyType#VALUE_FOUND} command back.
     *
     * @param peer           who requested the value
     * @param requestContent information about the value to find, must be parsed first
     */
    protected void onFindValueRequest(SMSPeer peer, String requestContent) {

    }

    /**
     * Method called when a STORE request is received.
     *
     * @param requestContent information about the key/value to store, must be parsed.
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
     * Method called when a NODE_FOUND reply is received.
     *
     * @param content      contains the kad address and the k closest nodes found to it
     */
    protected void onNodeFoundReply(String content) {
        String[] splitStr = content.split(SMSCommandMapper.SPLIT_CHAR);
        //to call after content has been parsed: onNodeFound(SMSKADPeer) if k = 1
    }

    protected void onNodeFoundReply(SMSKADPeer closestReceived){
        //TODO change second parameter to list if we expect to receive k nodes as a response, now k = 1
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
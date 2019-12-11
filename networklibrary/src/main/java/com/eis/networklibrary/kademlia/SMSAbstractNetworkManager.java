package com.eis.networklibrary.kademlia;

import com.eis.communication.network.NetworkManager;
import com.eis.communication.network.SerializableObject;
import com.eis.communication.network.kademlia.KADPeer;
import com.eis.smslibrary.SMSHandler;
import com.eis.smslibrary.SMSMessage;
import com.eis.smslibrary.SMSPeer;

import java.util.ArrayList;


/**
 * This class is intended to be extended by the specific application. It is an implementation of NetworkManager.
 *
 * @author Marco Mariotto
 * @author Alessandra Tonin
 * @author Alberto Ursino
 */
public abstract class SMSAbstractNetworkManager implements NetworkManager<SMSKADPeer, SerializableObject, SerializableObject> {

    final static String SPLIT_CHAR = "_";
    protected String networkName;
    protected SMSKADPeer mySelf;
    private SMSDistributedNetworkDictionary<SerializableObject> dict;
    //joinSent keeps track of JOIN_PROPOSAL requests still pending.
    private ArrayList<SMSPeer> joinSent = new ArrayList<>();
    //This class makes use of SMSHandler to send requests
    private SMSHandler handler;

    private ReplyListener resourceListener;

    protected static final String[] REPLIES = {
            Reply.PING_ECHO.toString(),
            Reply.NODE_FOUND.toString(),
            Reply.VALUE_FOUND.toString(),
            Reply.JOIN_AGREED.toString()
    };

    protected static final String[] REQUESTS = {
            Request.JOIN_PROPOSAL.toString(),
            Request.PING.toString(),
            Request.STORE.toString(),
            Request.FIND_NODE.toString(),
            Request.FIND_VALUE.toString()
    };

    /**
     * Sets up a new network
     *
     * @param handler     Handler to set for sending requests
     * @param networkName Name of the network being created
     * @param mySelf      The current peer executing setup()
     */
    public void setup(SMSHandler handler, String networkName, SMSPeer mySelf) {
        this.handler = handler;
        this.networkName = networkName;
        this.mySelf = new SMSKADPeer(mySelf);
        dict = new SMSDistributedNetworkDictionary(new SMSKADPeer(mySelf));
    }

    /**
     * Sends an invitation to the specified peer
     *
     * @param peer The peer of the user to invite
     */
    @Override
    public void invite(SMSKADPeer peer) {
        SMSMessage invitation = new SMSMessage(peer, buildRequest(Request.JOIN_PROPOSAL, networkName));
        joinSent.add(peer);
        handler.sendMessage(invitation);
    }

    /**
     * Inserts the {@link SMSMessage} peer in the network
     *
     * @param invitation The invitation message
     */
    public void join(SMSMessage invitation) {
        //TODO Take a look to this method
        dict.addUser(new SMSKADPeer(invitation.getPeer()));
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

        /*
         * Create the KADAddress of the resource
         */
        KADAddress resKadAddress = new KADAddress(key.toString());

        /*
         * Find the resource's bucket
         * //TODO gestire il caso del bucket inesistente
         */
        int bucketIndex = mySelf.getNetworkAddress().firstDifferentBit(resKadAddress);

        /*
         * Get all users which are in the resource's bucket
         */
        ArrayList<SMSKADPeer> resCandidates = dict.getUsersInBucket(bucketIndex);

        /*
         * Compare users' addresses with resource's address to find the closest
         */
        //TODO

    }

    /**
     * Removes a key-value resource from the local dictionary: this is called if a STORE (key, NULL) message is received
     *
     * @param key The resource key
     */
    @Override
    public void removeResource(SerializableObject key) {
        setResource(key, null);
    }

    /**
     * Finds the peer of the given KADAddress object
     * If the given KADAddress doesn't exist then finds then peer of the closest (to the one given)
     *
     * @param kadAddress The KADAddress for which to find the peer
     * @return an {@link KADAddress} object
     */
    private KADPeer findAddressPeer(KADAddress kadAddress) {
        //TODO this only ask the closest node in our local dictionary to find closer node to the given KADAddress
        //TODO 1. Trovare il bucket in cui si trova il kadAddress o, se non esiste, quello più vicino ad esso
        //TODO 2. Confrontare gli indirizzi degli utenti di quel bucket con quello del kadAddress e prendo il più vicino
        //TODO 3. Estrapolare il peer dall'indirizzo trovato e ritornarlo
        return null;
    }


    /**
     * Republishes a key to be retained in the network
     *
     * @param key The key to be republished
     */
    public void republishKey(SerializableObject key) {
        //TODO Take a look to this method
        setResource(key, dict.getValue(new KADAddress(key.toString())));
    }

    /**
     * Method used to find a value of the given key
     *
     * @param resourceKey The resource key of which we want to find the value
     */
    @Override
    public void findValue(SerializableObject resourceKey, ReplyListener listener) {
        KADAddress key = new KADAddress(resourceKey.toString());
        SerializableObject value = dict.getValue(key);
        if (value != null) {
            listener.onValueReceived(value);
            return;
        }
        ArrayList<SMSKADPeer> peersThatMightHaveTheRes = dict.getUsersInBucket(key.firstDifferentBit(mySelf.networkAddress));
        if (peersThatMightHaveTheRes.size() == 0) {
            listener.onValueNotFound();
            return;
        }
        for (SMSKADPeer possiblePeer : peersThatMightHaveTheRes) {
            SMSMessage invMsg = new SMSMessage(possiblePeer, buildRequest(Request.FIND_VALUE, resourceKey.toString()));
            handler.sendMessage(invMsg);
        }
        resourceListener = listener;
    }

    /**
     * Construction of specific objects for resource keys cannot be done here. It is up to the application to override this method.
     *
     * @param key The string key
     */
    protected abstract SerializableObject getKeyFromString(String key);

    /**
     * Construction of specific objects for resource values cannot be done here. It is up to the application to override this method.
     *
     * @param value The string value
     */
    protected abstract SerializableObject getValueFromString(String value);

    /**
     * Builds a request
     *
     * @param req  The request name
     * @param args TODO
     * @return TODO
     */
    private String buildRequest(Request req, String... args) {
        String requestStr = "";
        switch (req) {
            case JOIN_PROPOSAL:
                requestStr = Request.JOIN_PROPOSAL.toString() + SPLIT_CHAR + "%s";
                break;
            case PING:
                requestStr = Request.PING.toString() + SPLIT_CHAR + "%s";
                break;
            case FIND_NODE:
                requestStr = Request.FIND_NODE.toString() + SPLIT_CHAR + "%s";
                break;
            case FIND_VALUE:
                requestStr = Request.FIND_VALUE.toString() + SPLIT_CHAR + "%s";
                break;
            case STORE:
                requestStr = Request.STORE.toString() + SPLIT_CHAR + "%s" + SPLIT_CHAR + "%s";
                break;
        }
        return String.format(requestStr, args);
    }

    /**
     * It processes every message: could be a reply or a request performing changes to the local dictionary.
     * Invalid formats should not be received, now are silently discarded.
     *
     * @param message containing the request to be processed
     */
    void processMessage(SMSMessage message) {
        String[] splitMessageContent = message.getData().split(SPLIT_CHAR, 2);
        String messagePrefix = splitMessageContent[0];
        for (Reply replyCommand : Reply.values()) {
            if (replyCommand.toString().equals(messagePrefix)) {
                processReply(replyCommand, splitMessageContent[1]);
                return;
            }
        }
        for (Request requestCommand : Request.values()) {
            if (requestCommand.toString().equals(messagePrefix)) {
                processRequest(requestCommand, splitMessageContent[1]);
                return;
            }
        }
        //SHOULD NEVER GET HERE
        throw new IllegalStateException("Could not parse command prefix");
    }

    /**
     * TODO
     *
     * @param req            TODO
     * @param commandContent TODO
     */
    private void processRequest(Request req, String commandContent) {
        switch (req) {
            case JOIN_PROPOSAL:
                //onJoinProposal(commandContext correctly processed)
                //Even though it's already called from the listener
                //TODO: remove this call from the SMSAbstractNetworkListener, there should be another listener for this
                break;
        }
    }

    /**
     * TODO
     *
     * @param reply          TODO
     * @param commandContent TODO
     */
    private void processReply(Reply reply, String commandContent) {
        switch (reply) {
            case JOIN_AGREED:
                //onJoinAgreed()
                break;
            //TODO: Fill in with all the replies and call the right method
        }
    }

    enum Request {
        JOIN_PROPOSAL,
        PING,
        STORE,
        FIND_NODE,
        FIND_VALUE
    }

    enum Reply {
        JOIN_AGREED,
        PING_ECHO,
        NODE_FOUND,
        VALUE_FOUND
    }
}
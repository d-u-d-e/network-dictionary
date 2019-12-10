package com.eis.networklibrary.kademlia;


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
 * @authore Alberto Ursino
 * @author Alessandra Tonin
 */
public abstract class SMSAbstractNetworkManager /*implements NetworkManager<SMSKADPeer, SerializableObject, SerializableObject>*/ {
    final static String SPLIT_CHAR = "_";
    protected String networkName;
    protected SMSKADPeer mySelf;
    private SMSDistributedNetworkDictionary<SerializableObject> dict;
    //joinSent keeps track of JOIN_PROPOSAL requests still pending.
    private ArrayList<SMSPeer> joinSent = new ArrayList<>();
    //manager makes use of SMSHandler to send requests
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
     * @param handler     we set up a handler for sending requests
     * @param networkName of the network being created
     * @param mySelf      the current peer executing setup()
     */
    public void setup(SMSHandler handler, String networkName, SMSPeer mySelf) {
        this.handler = handler;
        this.networkName = networkName;
        //mySelf is the current peer setting up the network
        this.mySelf = new SMSKADPeer(mySelf);
        dict = new SMSDistributedNetworkDictionary(new SMSKADPeer(mySelf));
    }

    /**
     * Sends an invitation to the specified peer
     *
     * @param peer who is asked to join the network
     */
    public void invite(SMSPeer peer) {
        SMSMessage invMsg = new SMSMessage(peer, buildRequest(Request.JOIN_PROPOSAL, networkName));
        joinSent.add(peer);
        handler.sendMessage(invMsg);
    }

    /**
     * //TODO
     * @param invitation
     */

    public void join(SMSMessage invitation){

    }

    /**
     * Sets a (key, value) resource for the local dictionary: this is called only if a STORE message is received
     *
     * @param key   The resource key
     * @param value The resource value
     */
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
        

    }

    /**
     * Removes a key-value resource from the local dictionary: this is called if a STORE (key, NULL) message is received
     *
     * @param key The Key for which to set the value to null
     */
    public void removeResource(SerializableObject key) {
        setResource(key, null);
    }

    /**
     * Method used to finds the peer of the given KADAddress object
     * If the given KADAddress doesn't exist then finds then peer of the closest (to the one given)
     *
     * @param kadAddress The KADAddress for which to find the peer
     * @return the found peer
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
     * @param key to be republished
     */
    public void republishKey(SerializableObject key) {
        //TODO 1. Serve praticamente chiamare di nuovo setResource
    }

    /**
     * Find value of key
     * Sends a request
     *
     * @param resourceKey of which we want to find the value
     */
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
     * @param key as string
     */
    protected abstract SerializableObject getKeyFromString(String key);

    /**
     * Construction of specific objects for resource values cannot be done here. It is up to the application to override this method.
     *
     * @param value as string
     */
    protected abstract SerializableObject getValueFromString(String value);

    /**
     * SPLIT_CHAR = '_' is used to split fields in each request or reply
     * <p>
     * SMS REQUESTS FORMATS
     * JOIN proposal:      "JP_%netName"            netName is the name of the network the new node is asked to join
     * PING request:       "PI_%(randomId)"         randomId is an identifier to match ping requests with replies
     * STORE request:      "ST_%(key)_%(value)"
     * FIND_NODE request:  "FN_%(KADAddress)"          find the K-CLOSEST nodes to this KAD peer (we want to know their phone numbers)
     * FIND_VALUE request: "FV_%(key)
     * <p>
     * <p>
     * SMS REPLIES FORMATS
     * JOIN agreed:       "PJ_%netName" //we use the same notation to keep it consistent with NF and VF
     * PING reply:        "IP_%(matchingId)"
     * NODE_FOUND reply:  "NF_%(phoneNumber)_%(KADAddress)"  TODO how many entries should we pack inside this reply?
     * VALUE_FOUND reply: "VF_%(key)_(value)" TODO should send also key to mach with value? Or use a randomId like in PING?
     */

    private String buildRequest(Request req, String... args) {
        String requestStr = "";
        switch (req) {
            case JOIN_PROPOSAL:
                requestStr = Request.JOIN_PROPOSAL + SPLIT_CHAR + "%s";
                break;
            case PING:
                requestStr = Request.PING + SPLIT_CHAR + "%s";
                break;
            case FIND_NODE:
                requestStr = Request.FIND_NODE + SPLIT_CHAR + "%s";
                break;
            case FIND_VALUE:
                requestStr = Request.FIND_VALUE + SPLIT_CHAR + "%s";
                break;
            case STORE:
                requestStr = Request.STORE + SPLIT_CHAR + "%s" + SPLIT_CHAR + "%s";
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

    private void processRequest(Request req, String commandContent) {
        switch (req) {
            case JOIN_PROPOSAL:
                //onJoinProposal(commandContext correctly processed)
                //Even though it's already called from the listener
                //TODO: remove this call from the listener, there should be another listener for this
                break;
        }
    }

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
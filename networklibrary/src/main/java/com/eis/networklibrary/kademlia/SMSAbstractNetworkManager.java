package com.eis.networklibrary.kademlia;


import com.eis.communication.network.SerializableObject;
import com.eis.smslibrary.SMSHandler;
import com.eis.smslibrary.SMSMessage;
import com.eis.smslibrary.SMSPeer;

import java.util.ArrayList;


/**
 * This class is intended to be extended by the specific application. It is an implementation of NetworkManager.
 *
 * @author Marco Mariotto
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

    private RobertoListener resourceListener;

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
        SMSMessage invMsg = new SMSMessage(peer, SMSCommandMapper.buildRequest(Request.JOIN_PROPOSAL, networkName));
        joinSent.add(peer);
        handler.sendMessage(invMsg);
    }


    public void join(SMSMessage invitation){

    }

    /**
     * Sets a key-value resource for the local dictionary: this is called only if a STORE message is received
     *
     * @param key   resource key
     * @param value resource value
     */
    public void setResource(SerializableObject key, SerializableObject value) {
        //TODO Find the closest node and tell to it to store the (key, value) pair
        //TODO 1. Trovare il bucket in cui si trova la risorsa o, se non esiste, quello più vicino ad essa
        //TODO 2. Confronto gli indirizzi degli utenti di quel bucket con quello della risorsa e prendo il più vicino
        //TODO 3. Rendo responsabile della risorsa l'utente trovato al punto 2
    }

    /**
     * Removes a key-value resource from the local dictionary: this is called if a STORE (key, NULL) message is received
     *
     * @param key resource key
     */
    public void removeResource(SerializableObject key) {
        setResource(key, null);
    }

    private void findClosestNodes(KADAddress peer) {
        //TODO this only ask the k-closest nodes in our local dictionary to find closer nodes to peer.
    }

    /**
     * It processes every message: could be a reply or a request performing changes to the local dictionary.
     * Invalid formats should not be received, now are silently discarded.
     *
     * @param message containing the request to be processed
     */
    void processMessage(SMSMessage message) {
        //TODO can be either a reply or a request
    }

    /**
     * Republishes a key to be retained in the network
     *
     * @param key to be republished
     */
    public void republishKey(SerializableObject key) {
        //TODO
    }

    /**
     * Find value of key
     * Sends a request
     *
     * @param resourceKey of which we want to find the value
     */
    public void findValue(SerializableObject resourceKey, RobertoListener listener) {
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
            SMSMessage invMsg = new SMSMessage(possiblePeer, SMSCommandMapper.buildRequest(Request.FIND_VALUE, resourceKey.toString()));
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
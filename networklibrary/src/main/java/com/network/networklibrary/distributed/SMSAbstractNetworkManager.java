package com.network.networklibrary.distributed;


import com.eis.smslibrary.SMSHandler;
import com.eis.smslibrary.SMSPeer;
import com.eis.smslibrary.SMSMessage;
import com.network.communication.NetworkManager;
import com.network.communication.SerializableObject;

import java.util.ArrayList;


/**
 * This class is intended to be extended by the specific application. It is an implementation of NetworkManager.
 * @author Marco Mariotto
 */
public abstract class SMSAbstractNetworkManager implements NetworkManager<SMSKADPeer, SerializableObject, SerializableObject>
{
    /**
     * SMS REQUESTS FORMATS
     * JOIN proposal:      "JP_%netName"            netName is the name of the network the new node is asked to join
     * PING request:       "PI_%(randomId)"         randomId is an identifier to match ping requests with replies
     * STORE request:      "ST_%(key)_%(value)"
     * FIND_NODE request:  "FN_%(KADPeerAddress)"   find the K-CLOSEST nodes to this address (we want to know their phone numbers)
     * FIND_VALUE request: "FV_%(key)
     */

    private SMSDistributedNetworkDictionary dict;
    //joinSent keeps track of JOIN_PROPOSAL requests still pending.
    private ArrayList<SMSKADPeer> joinSent = new ArrayList<>();
    protected String networkName;
    //manager makes use of SMSHandler to send requests
    private SMSHandler handler;
    protected SMSPeer mySelf;

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
        this.mySelf = mySelf;
        dict = new SMSDistributedNetworkDictionary(new SMSKADPeer(mySelf));
    }

    /**
     * Sends an invitation to the specified peer
     *
     * @param peer who is asked to join the network
     */
    public void invite(SMSPeer peer) {

    }

    /**
     * Sets a key-value resource for the local dictionary
     *
     * @param key   resource key
     * @param value resource value
     */
    public void setResource(SerializableObject key, SerializableObject value) {

    }

    /**
     * Removes a key-value resource from the local dictionary
     *
     * @param key resource key
     */
    public void removeResource(SerializableObject key) {

    }

    /**
     * It processes every request performing changes to the local dictionary.
     *
     * @param message containing the request to be processed
     */
    void processRequest(SMSMessage message) {

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
}
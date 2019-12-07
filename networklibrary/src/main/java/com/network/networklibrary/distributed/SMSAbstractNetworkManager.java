package com.network.networklibrary.distributed;


import com.eis.smslibrary.SMSPeer;
import com.eis.smslibrary.SMSMessage;
import com.network.communication.NetworkManager;
import com.network.communication.SerializableObject;


/**
 * This class is intended to be extended by the specific application. It is an implementation of NetworkManager.
 * @author Marco Mariotto
 */
public abstract class SMSAbstractNetworkManager implements NetworkManager<SMSPeer, SerializableObject, SerializableObject> {

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

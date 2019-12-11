package com.eis.communication.network;

import com.eis.communication.Peer;
import com.eis.networklibrary.kademlia.ReplyListener;

/**
 * @param <U>  Peer for users in the network
 * @param <RK> Key class for the resource
 * @param <RV> Value class for the resource
 * @author Marco Mariotto
 * @author Alberto Ursino
 * @since 10/12/2019
 */
public interface NetworkManager<U extends Peer, RK extends SerializableObject, RV extends SerializableObject> {

    /**
     * Sends an invitation to the specified peer
     *
     * @param user The peer of the user to invite
     */
    void invite(U user);

    /**
     * Sets a (key, value) resource in the local dictionary: this is called only if a STORE message is received
     *
     * @param key   The resource key
     * @param value The resource value
     */
    void setResource(RK key, RV value);

    /**
     * Removes a key-value resource from the local dictionary: this is called if a STORE (key, NULL) message is received
     *
     * @param key The resource key
     */
    void removeResource(RK key);

    /**
     * Method used to find a value of the given key
     *
     * @param key      The resource key
     * @param listener The listener used to warn when the value was found
     */
    void findValue(RK key, ReplyListener listener);

}
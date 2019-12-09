package com.eis.communication;

import com.eis.networklibrary.distributed.KADPeer;

/**
 * @param <U>  Peer for users in the network
 * @param <RK> Key class identifier for the resource
 * @param <RV> Value class for the resource
 * @author Marco Mariotto
 */

public interface NetworkManager<U extends KADPeer, RK, RV> {
    /**
     * sends an invitation message to a user to let him join this network
     *
     * @param user who receives the invitation
     */
    void invite(U user);

    /**
     * sets a key-value resource, telling the k-closest nodes to store it
     *
     * @param key   resource key
     * @param value resource value
     */
    void setResource(RK key, RV value);

    /**
     * removes a key-value resource, telling the k-closest nodes who keep it to remove it
     *
     * @param key resource key
     */
    void removeResource(RK key);

    /**
     * republishes a valid key
     *
     * @param key resource key
     */
    void republishKey(RK key);

    /**
     * find the value of key
     *
     * @param key resource key
     */
    void findValue(RK key);

    //TODO how to handle disconnect()?
}
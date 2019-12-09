package com.eis.communication;

/**
 * @param <U>  Peer for users in the network
 * @param <RK> Key class identifier for the resource
 * @param <RV> Value class for the resource
 * @author Marco Mariotto
 */

public interface NetworkManager<U extends Peer, RK, RV> {
    /**
     * send an invitation message to a user to let him join this network
     *
     * @param user who receives the invitation
     */
    void invite(U user);

    /**
     * inform every user in the network that the current application is disconnecting from the network
     */
    void disconnect();

    /**
     * set a key-value resource for the local dictionary and spread this information to every user
     *
     * @param key   resource key
     * @param value resource value
     */
    void setResource(RK key, RV value);

    /**
     * remove a key-value resource from the local dictionary and spread this information to every user
     *
     * @param key resource key
     */
    void removeResource(RK key);
}

package com.eis.communication.network;

import com.eis.communication.Peer;

/**
 * @param <U>  Peer for users in the network
 * @param <RK> Key class for the resource
 * @param <RV> Value class for the resource
 * @author Marco Mariotto
 * @author Alberto Ursino
 * @author Luca Crema
 * @since 10/12/2019
 */
public interface NetworkManager<U extends Peer, RK extends SerializableObject, RV extends SerializableObject, I extends Invitation> {

    /**
     * Sends an invitation to the specified peer.
     *
     * @param user The peer of the user to invite.
     */
    void invite(U user);

    /**
     * Adds the user to the network, if they were invited.
     *
     * @param invitation The received invitation to the network.
     */
    void join(I invitation);

    /**
     * Sets a (key, value) resource in the local dictionary: this is called only if a STORE message is received
     *
     * @param key        The resource key
     * @param value      The resource value
     * @param maxWaiting Maximum milliseconds to wait before considering this request unsuccessful.
     *                   If maxWaiting is 0, no time limit is set.
     *                   maxWaiting minim value is 60 seconds.
     * @param listener   The listener that has to be called when maxWaiting time is over
     */
    void setResource(RK key, RV value, int maxWaiting, ResourceListener listener);

    /**
     * Removes a key-value resource from the local dictionary: this is called if a STORE (key, NULL) message is received
     *
     * @param key The resource key
     */
    void removeResource(RK key);

    /**
     * Method used to find a value of the given key
     *
     * @param key        The resource key
     * @param listener   The listener used to warn when the value was found
     * @param maxWaiting Maximum milliseconds to wait before considering this request unsuccessful. If maxWaiting is 0, no time limit is set.
     */
    void findValue(RK key, FindValueListener listener, int maxWaiting);

    void setJoinProposalListener(JoinListener listener);
}
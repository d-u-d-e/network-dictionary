package com.eis.communication.network;

import com.eis.communication.Peer;

/**
 * Represents a received invitation to join a network. Can be overridden.
 *
 * @param <P> Type of peer of the network.
 */
public interface Invitation<P extends Peer> {

    /**
     * @return The peer who invited you to the network
     */
    P getInviter();

}

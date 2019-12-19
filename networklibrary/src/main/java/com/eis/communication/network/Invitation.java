package com.eis.communication.network;

import com.eis.communication.Peer;

/**
 * Represents a received invitation to join a network. Can be overridden.
 *
 * @param <P> Type of peer of the network.
 */
public class Invitation<P extends Peer> {
    protected  Peer inviter;
    protected  Peer guest;
}

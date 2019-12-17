package com.eis.communication.network;

import com.eis.communication.Peer;

/**
 * Listener used for requests to find a specific node in the network
 *
 * @author ?
 * @author Luca Crema
 * @since 16/12/2019
 */
public interface FindNodeListener<P extends Peer> {
    /**
     * Called when the peer of a given address is found.
     *
     * @param peer The peer reference.
     */
    void OnKClosestNodesFound(P peer);
}

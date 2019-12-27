package com.eis.communication.network;

import com.eis.communication.Peer;

/**
 * Listener called to handle findClosestNodes requests
 */
public interface FindNodeListener<P extends Peer> {
    /**
     * Called when the k-closest peers are received
     *
     * @param peers The k-closest peers received.
     */
    void OnKClosestNodesFound(P[] peers);

    /**
     * Called when the find request has timed out. Default implementation does nothing.
     */
    default void onFindTimedOut() {}
}

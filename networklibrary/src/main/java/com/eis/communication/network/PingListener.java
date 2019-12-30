package com.eis.communication.network;

import com.eis.smslibrary.SMSPeer;

/**
 * A listener waiting for a {@code PING} request
 *
 * @author Alessandra Tonin
 * <p>
 * CODE REVIEW FOR CROCIANI AND DE ZEN
 */
public interface PingListener {

    /**
     * Method called when a PingReply is received
     *
     * @param peer The peer who replied to a ping
     */
    void onPingReply(SMSPeer peer);

    /**
     * Method called when the timeout is expired, so a node didn't answer in time
     *
     * @param peer The peer who not replied to a ping
     */
    void onPingTimedOut(SMSPeer peer);
}

package com.eis.communication.network;

import com.eis.smslibrary.SMSPeer;

/**
 * A listener waiting for a {@code PING} request
 *
 * @author Alessandra Tonin
 *
 * CODE REVIEW FOR CROCIANI AND DE ZEN
 */
public interface PingListener {
    void onPingReply(SMSPeer peer);

    void onPingTimedOut(SMSPeer peer);
}

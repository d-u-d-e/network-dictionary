package com.eis.communication.network;

import com.eis.smslibrary.SMSPeer;

/**
 * A listener waiting for a {@code PING_ECHO} reply
 */
public interface PingListener {
    /**
     * @param peer to which the ping request was sent
     */
    void onPingReply(SMSPeer peer);

    /**
     * @param peer to which the ping request was sent
     */
    void onPingTimedOut(SMSPeer peer);
}

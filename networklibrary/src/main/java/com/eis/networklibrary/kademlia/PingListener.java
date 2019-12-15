package com.eis.networklibrary.kademlia;

import com.eis.smslibrary.SMSPeer;

/**
 * A listener waiting for a {@code PING} request
 *
 * @author Alessandra Tonin
 */
public interface PingListener {
    void onPingReply(SMSPeer peer);
}

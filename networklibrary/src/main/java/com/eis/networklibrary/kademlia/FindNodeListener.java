package com.eis.networklibrary.kademlia;

/**
 * A listener waiting for a {@code NODE_FOUND} reply
 *
 * */

interface FindNodeListener
{
    void onClosestNodeFound(SMSKADPeer peer);
}

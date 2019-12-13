package com.eis.networklibrary.kademlia;

interface FindNodeListener {

    void onClosestNodeFound(SMSKADPeer peer);
}

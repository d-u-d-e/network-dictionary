package com.eis.networklibrary.kademlia;

//has to implement this
interface SMSNetworkManagementListener {
    void onJoinAgreed();
    void onNodeFound(SMSKADPeer peer);
}

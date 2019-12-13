package com.eis.networklibrary.kademlia;

import com.eis.smslibrary.SMSPeer;

interface SMSNetworkCallbackListener {

    /**
     * This method is called when a join proposal is received. It should let the user
     * know they has been invited to join the network, and let them decide if they want to join.
     * {@link SMSNetworkManager#join} has to be called in order to join.
     *
     * @param inviter asking you to join the network
     */
    void onJoinRequest(SMSPeer inviter);

    void onValueFound();

    void onNodeFound(SMSKADPeer peer);
}

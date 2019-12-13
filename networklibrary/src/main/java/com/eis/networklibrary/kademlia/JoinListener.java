package com.eis.networklibrary.kademlia;

import com.eis.smslibrary.SMSPeer;

public interface JoinListener {
    void onJoinProposal(SMSPeer peer);

}

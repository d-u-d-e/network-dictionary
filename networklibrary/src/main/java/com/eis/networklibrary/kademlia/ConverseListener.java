package com.eis.networklibrary.kademlia;

import com.eis.communication.network.SerializableObject;
import com.eis.smslibrary.SMSPeer;

public interface ConverseListener {

    void onValueNotFound();
    void onValueFound(SerializableObject value);
    //if the user accepts to join, it has to call SMSNetworkManager@#join() and send a JOIN_AGREED reply
    void onJoinProposal(SMSPeer peer);
}

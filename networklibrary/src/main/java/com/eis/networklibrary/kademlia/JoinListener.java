package com.eis.networklibrary.kademlia;

import com.eis.smslibrary.SMSPeer;


/**
 * A listener waiting for a {@code JOIN_PROPOSAL} request
 *
 * */
public interface JoinListener
{
    void onJoinProposal(SMSPeer peer);
}

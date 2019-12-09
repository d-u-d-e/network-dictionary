package com.eis.networklibrary.distributed;

import com.eis.smslibrary.SMSMessage;
import com.eis.smslibrary.SMSPeer;
import com.eis.smslibrary.listeners.SMSReceivedServiceListener;


public abstract class SMSAbstractNetworkListener extends SMSReceivedServiceListener
{
    @Override
    public void onMessageReceived(SMSMessage message){
                /*TODO check if incoming request is valid: if it is a join req, call onJoinProposal(),
                  TODO otherwise call processMessage() of SMSAbstractNetworkManager
                */
    }

    /**
     * This method is called when a join proposal is received. It should let the user
     * know they has been invited to join the network, and let them decide if they want to join.
     * {@link SMSAbstractNetworkListener#join} has to be called in order to join.
     *
     * @param inviterPeer The peer who invited you to join the network
     */
    public abstract void onJoinProposal(SMSPeer inviterPeer); //TODO: Should this be changed to SMSKADPeer?

}
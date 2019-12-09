package com.eis.networklibrary.distributed;

import com.eis.smslibrary.SMSMessage;
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
     * This method is for handling a JoinProposal. It is up to the application to override it.
     *
     * @param message as SMSMessage
     */
    public abstract void onJoinProposal(SMSMessage message);

}
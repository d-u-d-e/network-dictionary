package com.eis.networklibrary.kademlia;

import com.eis.smslibrary.SMSMessage;
import com.eis.smslibrary.listeners.SMSReceivedServiceListener;

import static com.eis.networklibrary.kademlia.SMSCommandMapper.SPLIT_CHAR;

/**
 * This listener receives messages from the broadcast receiver and checks whether it's a message sent by
 * the network, then lets the {@link SMSCommandMapper} do the rest
 *
 * @author Marco Mariotto
 * @author Alessandra Tonin
 * @author Luca Crema
 */
class SMSNetworkListener extends SMSReceivedServiceListener {

    /**
     * Checks if the received message is a command for the kad dictionary, check which command
     * has been received and calls the {@link SMSCommandMapper} correct method.
     *
     * @param message the received message
     */
    @Override
    public void onMessageReceived(SMSMessage message) {
        String[] splitMessageContent = message.getData().split(SPLIT_CHAR, 2);
        if (splitMessageContent.length != 2) {
            //TODO: Throw exception? What if it's a message for another app?
            return;
        }
        String messagePrefix = splitMessageContent[0];
        //Check if it's a reply
        for (SMSNetworkManager.Reply replyCommand : SMSNetworkManager.Reply.values()) {
            if (replyCommand.toString().equals(messagePrefix)) {
                SMSCommandMapper.processReply(replyCommand, splitMessageContent[1]);
                return;
            }
        }
        //Check if it's a request
        for (SMSNetworkManager.Request requestCommand : SMSNetworkManager.Request.values()) {
            if (requestCommand.toString().equals(messagePrefix)) {
                SMSCommandMapper.processRequest(requestCommand, splitMessageContent[1]);
                return;
            }
        }

        //TODO: Should we keep the exception? What if a message for another app has arrived and contains no command?
        //throw new IllegalArgumentException("Unknown command received");
    }

}
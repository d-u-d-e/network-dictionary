package com.eis.networklibrary.kademlia;

import androidx.annotation.NonNull;

import com.eis.smslibrary.SMSMessage;
import com.eis.smslibrary.SMSPeer;
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

    ConverseListener listener;

    SMSNetworkListener(ConverseListener listener){
        this.listener = listener;
    }

    /**
     * Checks if the received message is a command for the kad dictionary, check which command
     * has been received and calls processReply or processRequest.
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
        for (SMSNetworkManager.ReplyType replyCommand : SMSNetworkManager.ReplyType.values()) {
            if (replyCommand.toString().equals(messagePrefix)) {
                processReply(replyCommand, message.getPeer(), splitMessageContent[1]);
                return;
            }
        }
        //Check if it's a request
        for (SMSNetworkManager.RequestType requestCommand : SMSNetworkManager.RequestType.values()) {
            if (requestCommand.toString().equals(messagePrefix)) {
                processRequest(requestCommand, message.getPeer(), splitMessageContent[1]);
                return;
            }
        }

        //TODO: Should we keep the exception? What if a message for another app has arrived and contains no command?
        //throw new IllegalArgumentException("Unknown command received");
    }


    private void processRequest(SMSNetworkManager.RequestType req, SMSPeer peer, String commandContent) {
        switch (req) {
            case JOIN_PROPOSAL:
                listener.onJoinProposal(peer);
                break;
            case PING:
                SMSNetworkManager.getInstance().onPingRequest(peer);
                break;
            case FIND_NODE:
                SMSNetworkManager.getInstance().onFindNodeRequest(peer, commandContent);
                break;
            case FIND_VALUE:
                SMSNetworkManager.getInstance().onFindValueRequest(peer, commandContent);
                break;
            case STORE:
                SMSNetworkManager.getInstance().onStoreRequest(peer, commandContent);
                break;
        }
    }

    /**
     * @param reply          The command received.
     * @param commandContent The content of the command without the command prefix, can be empty.
     */
    private void processReply(SMSNetworkManager.ReplyType reply, SMSPeer peer, String commandContent) {
        switch (reply) {
            case JOIN_AGREED:
                SMSNetworkManager.getInstance().onJoinAgreedReply(peer);
                break;
            case PING_ECHO:
                SMSNetworkManager.getInstance().onPingEchoReply(peer);
                break;
            case NODE_FOUND:
                SMSNetworkManager.getInstance().onNodeFoundReply(peer, commandContent);
                break;
            case VALUE_FOUND:
                SMSNetworkManager.getInstance().onValueFoundReply(peer, commandContent);
                break;
        }
    }

}
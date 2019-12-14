package com.eis.networklibrary.kademlia;

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

    JoinListener joinListener;

    SMSNetworkListener(JoinListener listener) {
        this.joinListener = listener;
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


    /**
     * Calls the appropriate method depending on the received request
     *
     * @param req            the request received
     * @param sender          the request sender
     * @param commandContent the content of the command without the command prefix, can be empty
     */
    private void processRequest(SMSNetworkManager.RequestType req, SMSPeer sender, String commandContent) {
        switch (req) {
            case JOIN_PROPOSAL:
                joinListener.onJoinProposal(sender);
                break;
            case PING:
                SMSNetworkManager.getInstance().onPingRequest(sender);
                break;
            case FIND_NODE:
                SMSNetworkManager.getInstance().onFindNodeRequest(sender, commandContent);
                break;
            case FIND_VALUE:
                SMSNetworkManager.getInstance().onFindValueRequest(sender, commandContent);
                break;
            case STORE:
                SMSNetworkManager.getInstance().onStoreRequest(commandContent);
                break;
        }
    }

    /**
     * Calls the appropriate method depending on the received reply
     *
     * @param reply          the command received
     * @param commandContent the content of the command without the command prefix, can be empty
     */
    private void processReply(SMSNetworkManager.ReplyType reply, SMSPeer peer, String commandContent) {
        SMSNetworkManager manager = SMSNetworkManager.getInstance();
        String[] splitStr = commandContent.split(SPLIT_CHAR);
        switch (reply) {
            case JOIN_AGREED:
                manager.onJoinAgreedReply(peer);
                break;
            case PING_ECHO:
                manager.onPingEchoReply(peer);
                break;
            case NODE_FOUND:
                manager.onNodeFoundReply(KADAddress.fromHexString(splitStr[0]), new SMSKADPeer(splitStr[1]), new SMSKADPeer(peer));
                break;
            case VALUE_FOUND:
                manager.onValueFoundReply(KADAddress.fromHexString(splitStr[0]), manager.valueParser.deSerialize(splitStr[1]));
                break;
        }
    }

}
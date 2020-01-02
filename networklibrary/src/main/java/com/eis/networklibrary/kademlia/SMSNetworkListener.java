package com.eis.networklibrary.kademlia;

import com.eis.smslibrary.SMSMessage;
import com.eis.smslibrary.SMSPeer;
import com.eis.smslibrary.listeners.SMSReceivedServiceListener;

import static com.eis.networklibrary.kademlia.SMSCommandMapper.SPLIT_CHAR;

/**
 * This listener receives messages from the broadcast receiver and checks whether it's a message sent by
 * the network, then lets the {@link SMSCommandMapper} do the rest.
 * <p>
 * <p>
 * SPLIT_CHAR is used to split fields in each request or reply
 * each KADAddress is sent as a hexadecimal string to spare characters
 * <p>
 * SMS REQUESTS FORMATS
 * JOIN proposal:        "JOIN_PROPOSAL-%netName"              netName is the name of the network the user receiving this is asked to join
 * PING request:         "PING"                                check whether the receiver is alive
 * STORE request:        "STORE-%(KADAddress key)-%(value)"    tell the receiver to store the (key, value) pair
 * DELETE request:       "DELETE-%(KADAddress key)"            tell the receiver to delete the (key, value) pair
 * FIND_NODES request:   "FIND_NODES-%(KADAddress address)"    find the K-CLOSEST nodes to this KAD address (we want to know their phone numbers)
 * FIND_VALUE request:   "FIND_VALUE-%(KADAddress key)         find the value associated with key
 * <p>
 * SMS REPLIES FORMATS
 * JOIN agreed:            "JOIN_AGREED" join confirmation
 * PING reply:             "PING_ECHO"   ping reply
 * NODES_FOUND reply:      "NODES_FOUND-%(KADAddress address)-(phoneNumber1)-...-(phoneNumber K)" the receiving user is told the K closer nodes to address
 * VALUE_NOT_FOUND reply:  "VALUE_NOT_FOUND-%(KADAddress key)-(phoneNumber1)-...-(phoneNumber K)" the receiving user is told other K closer nodes to the key, if the sender doesn't own the value
 * VALUE_FOUND reply:      "VALUE_FOUND-%(KADAddress key)-(value)"  the value for key is returned to the querier
 *
 * @author Marco Mariotto
 * @author Alessandra Tonin
 * @author Luca Crema
 */
class SMSNetworkListener extends SMSReceivedServiceListener {

    private final static int PREFIX = 0;
    private final static String EMPTY_STRING = "";

    /**
     * Checks if the received message is a command for the kad dictionary, checks which command
     * has been received and calls processReply or processRequest.
     *
     * @param message the received message
     */
    @Override
    public void onMessageReceived(SMSMessage message) {
        String[] splitMessageContent = message.getData().split(SPLIT_CHAR);
        String messagePrefix = splitMessageContent[PREFIX];
        int parts = splitMessageContent.length;
        //Check if it's a reply
        for (SMSNetworkManager.ReplyType replyCommand : SMSNetworkManager.ReplyType.values()) {
            if (replyCommand.toString().equals(messagePrefix)) {
                processReply(replyCommand, message.getPeer(), parts == 1 ? EMPTY_STRING : splitMessageContent[1]);
                return;
            }
        }
        //Check if it's a request
        for (SMSNetworkManager.RequestType requestCommand : SMSNetworkManager.RequestType.values()) {
            if (requestCommand.toString().equals(messagePrefix)) {
                processRequest(requestCommand, message.getPeer(), parts == 1 ? EMPTY_STRING : splitMessageContent[1]);
                return;
            }
        }
        //ignore other message formats
    }

    /**
     * Calls the appropriate method depending on the received request
     *
     * @param req            the request received
     * @param sender         the request sender
     * @param commandContent the content of the command without the command prefix, can be empty
     */
    private void processRequest(SMSNetworkManager.RequestType req, SMSPeer sender, String commandContent) {
        SMSNetworkManager manager = SMSNetworkManager.getInstance();
        switch (req) {
            case JOIN_PROPOSAL:
                manager.onJoinProposal(new KADInvitation(new SMSKADPeer(sender), manager.mySelf, commandContent));
                break;
            case PING:
                manager.onPingRequest(sender);
                break;
            case FIND_NODES:
                manager.onFindCloserNodesRequest(sender, commandContent);
                break;
            case FIND_VALUE:
                manager.onFindValueRequest(sender, commandContent);
                break;
            case STORE:
                manager.onStoreRequest(commandContent);
                break;

        }
    }

    /**
     * Calls the appropriate method depending on the received reply
     *
     * @param reply          the command received
     * @param commandContent the content of the command without the command prefix, can be empty
     */
    private void processReply(SMSNetworkManager.ReplyType reply, SMSPeer sender, String commandContent) {
        SMSNetworkManager manager = SMSNetworkManager.getInstance();
        switch (reply) {
            case JOIN_AGREED:
                manager.onJoinAgreedReply(sender);
                break;
            case PING_ECHO:
                manager.onPingEchoReply(sender);
                break;
            case NODES_FOUND:
                manager.onCloserNodesFoundReply(commandContent);
            case VALUE_NOT_FOUND:
                manager.onValueNotFoundReply(commandContent);
                break;
            case VALUE_FOUND:
                manager.onValueFoundReply(commandContent);
                break;
        }
    }

}
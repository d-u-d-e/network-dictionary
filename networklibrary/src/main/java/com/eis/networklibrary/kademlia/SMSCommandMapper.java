package com.eis.networklibrary.kademlia;

import com.eis.networklibrary.kademlia.SMSAbstractNetworkManager.Reply;
import com.eis.networklibrary.kademlia.SMSAbstractNetworkManager.Request;
import com.eis.smslibrary.SMSHandler;
import com.eis.smslibrary.SMSMessage;
import com.eis.smslibrary.SMSPeer;
import com.eis.smslibrary.listeners.SMSSentListener;

class SMSCommandMapper {

    final static String SPLIT_CHAR = "_";
    private static SMSHandler handler = SMSHandler.getInstance();

    public static void sendRequest(Request req, String content, SMSPeer peer, SMSSentListener sentListener) {
        SMSMessage request = new SMSMessage(peer, buildRequest(req, content));
        handler.sendMessage(request, sentListener);
    }

    public static void sendRequest(Request req, String content, SMSPeer peer) {
        sendRequest(req, content, peer, null);
    }

    /**
     * Builds a request
     *
     * @param req     The request name
     * @param content TODO
     * @return TODO
     */
    private static String buildRequest(Request req, String content) {
        return req.toString() + SPLIT_CHAR + content;
    }

    /**
     * It processes every message: could be a reply or a request performing changes to the local dictionary.
     * Invalid formats should not be received because they are silently discarded.
     *
     * @param message containing the request to be processed
     */
    protected static void processMessage(SMSMessage message) {
        String[] splitMessageContent = message.getData().split(SPLIT_CHAR, 2);
        String messagePrefix = splitMessageContent[0];
        //Check if it's a reply
        for (Reply replyCommand : Reply.values()) {
            if (replyCommand.toString().equals(messagePrefix)) {
                processReply(replyCommand, splitMessageContent[1]);
                return;
            }
        }
        //Check if it's a request
        for (Request requestCommand : Request.values()) {
            if (requestCommand.toString().equals(messagePrefix)) {
                processRequest(requestCommand, splitMessageContent[1]);
                return;
            }
        }
        //SHOULD NEVER GET HERE, CAN'T RECEIVE UNKNOWN COMMANDS
        //TODO: Decide whether we should let this go and do nothing or throw an exception
        throw new IllegalStateException("Could not parse command prefix");
    }

    /**
     * TODO
     *
     * @param req            TODO
     * @param commandContent TODO
     */
    private static void processRequest(Request req, String commandContent) {
        switch (req) {
            case JOIN_PROPOSAL:
                //onJoinProposal(commandContext correctly processed)
                //Even though it's already called from the listener
                //TODO: remove this call from the SMSAbstractNetworkListener, there should be another listener for this
                break;
        }
    }

    /**
     * TODO
     *
     * @param reply          TODO
     * @param commandContent TODO
     */
    private static void processReply(Reply reply, String commandContent) {
        switch (reply) {
            case JOIN_AGREED:
                //onJoinAgreed()
                break;
            //TODO: Fill in with all the replies and call the right method
        }
    }

}

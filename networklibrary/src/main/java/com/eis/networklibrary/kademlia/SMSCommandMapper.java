package com.eis.networklibrary.kademlia;

import com.eis.networklibrary.kademlia.SMSNetworkManager.Reply;
import com.eis.networklibrary.kademlia.SMSNetworkManager.Request;
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
     * TODO
     *
     * @param req            TODO
     * @param commandContent TODO
     */
    protected static void processRequest(Request req, String commandContent) {
        switch (req) {
            case JOIN_PROPOSAL:
                //onJoinProposal(commandContext correctly processed)
                //Even though it's already called from the listener
                //TODO: remove this call from the SMSNetworkListener, there should be another listener for this
                break;
        }
    }

    /**
     * TODO
     *
     * @param reply          TODO
     * @param commandContent TODO
     */
    protected static void processReply(Reply reply, String commandContent) {
        switch (reply) {
            case JOIN_AGREED:
                //onJoinAgreed()
                break;
            //TODO: Fill in with all the replies and call the right method
        }
    }

}

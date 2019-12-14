package com.eis.networklibrary.kademlia;

import com.eis.networklibrary.kademlia.SMSNetworkManager.ReplyType;
import com.eis.networklibrary.kademlia.SMSNetworkManager.RequestType;
import com.eis.smslibrary.SMSHandler;
import com.eis.smslibrary.SMSMessage;
import com.eis.smslibrary.SMSPeer;
import com.eis.smslibrary.listeners.SMSSentListener;

/**
 * @author Luca Crema
 * @author Marco Mariotto
 * <p>
 * /**
 * * SPLIT_CHAR = '-' is used to split fields in each request or reply
 * * each KADAddress is sent as a hexadecimal string to spare characters
 * * <p>
 * * SMS REQUESTS FORMATS
 * * JOIN proposal:      "JOIN_PROPOSAL-%netName"                netName is the name of the network the user receiving this is asked to join
 * * PING request:       "PING-%(randomId)"                      randomId is an identifier to match ping requests with replies
 * * STORE request:      "STORE-%(KADAddress key)-%(value)"      tell the receiver to store a (key, value) pair
 * * FIND_NODE request:  "FIND_NODE-%(KADAddress addr)"          find the K-CLOSEST nodes to this KAD address (we want to know their phone numbers)
 * * FIND_VALUE request: "FIND_VALUE-%(KADAddress key)           find the value associated with key
 * * <p>
 * * <p>
 * * SMS REPLIES FORMATS
 * * JOIN agreed:           "JOIN_AGREED"                            a join confirmation
 * * PING reply:            "PING_ECHO-%(matchingId)"                matchingId is to match this ping reply with its request
 * * NODE_FOUND reply:      "NODE_FOUND-%(KADAddress addr)-(phoneNumber1)-(phoneNumber2)...-(phoneNumber K)" //the receiving user is told other K closer nodes to addr
 *   TODO how many entries should we pack inside this reply?
 * * VALUE_FOUND reply:     "VALUE_FOUND-%(KADAddress key)-(value)"  the value for key is returned to the querier
 * * VALUE_NOT_FOUND reply: "VALUE_NOT_FOUND-%(KADAddress key)       the value for key has not been found
 */


@SuppressWarnings({"WeakerAccess", "unused"})
class SMSCommandMapper {

    final static String SPLIT_CHAR = "-";
    private static SMSHandler handler = SMSHandler.getInstance();

    /**
     * Sends an sms with the request.
     *
     * @param req          the request type.
     * @param content      the data of the request.
     * @param peer         the recipient of the request.
     * @param sentListener callback for when the message is sent.
     */
    public static void sendRequest(RequestType req, String content, SMSPeer peer, SMSSentListener sentListener) {
        SMSMessage messageRequest = new SMSMessage(peer, buildRequest(req, content));
        handler.sendMessage(messageRequest, sentListener);
    }

    /**
     * Sends an sms with the request.
     *
     * @param req     the request type.
     * @param content the data of the request.
     * @param peer    the recipient of the request.
     */
    public static void sendRequest(RequestType req, String content, SMSPeer peer) {
        sendRequest(req, content, peer, null);
    }

    /**
     * Sends an sms with the reply.
     *
     * @param reply        the reply type.
     * @param content      the data of the reply.
     * @param peer         the recipient of the reply.
     * @param sentListener callback for when message is sent.
     */
    public static void sendReply(ReplyType reply, String content, SMSPeer peer, SMSSentListener sentListener) {
        SMSMessage messageReply = new SMSMessage(peer, buildReply(reply, content));
        handler.sendMessage(messageReply, sentListener);
    }

    /**
     * Sends an sms with the reply.
     *
     * @param reply   the reply type.
     * @param content the data of the reply.
     * @param peer    the recipient of the reply.
     */
    public static void sendReply(ReplyType reply, String content, SMSPeer peer) {
        sendReply(reply, content, peer, null);
    }

    /**
     * Sends an sms with the reply. Useful for those replies that don't need content.
     *
     * @param reply        the reply type.
     * @param peer         the recipient of the reply.
     * @param sentListener callback for when message is sent.
     */
    public static void sendReply(ReplyType reply, SMSPeer peer, SMSSentListener sentListener) {
        SMSMessage messageReply = new SMSMessage(peer, buildReply(reply));
        handler.sendMessage(messageReply, sentListener);
    }

    /**
     * Sends an sms with the reply. Useful for those replies that don't need content.
     *
     * @param reply the reply type.
     * @param peer  the recipient of the reply.
     */
    public static void sendReply(ReplyType reply, SMSPeer peer) {
        sendReply(reply, peer, null);
    }

    /**
     * Parses a request into a string to put into a message.
     *
     * @param req     The request.
     * @param content The content of the request.
     * @return The request parsed into a string ready to be sent.
     */
    private static String buildRequest(RequestType req, String content) {
        return req.toString() + SPLIT_CHAR + content;
    }

    /**
     * Parses a reply into a string to put into a message.
     *
     * @param reply   The reply.
     * @param content The content of the reply.
     * @return The reply parsed into a string ready to be sent. Can't be null, if you don't have content use {@link #buildReply(ReplyType)}
     */
    private static String buildReply(ReplyType reply, String content) {
        return reply.toString() + SPLIT_CHAR + content;
    }

    /**
     * Parses a reply into a string to put into a message.
     * This method is for those replies that don't use the content.
     *
     * @param reply The reply.
     * @return The reply parsed into a string ready to be sent.
     */
    private static String buildReply(ReplyType reply) {
        return reply.toString();
    }

}

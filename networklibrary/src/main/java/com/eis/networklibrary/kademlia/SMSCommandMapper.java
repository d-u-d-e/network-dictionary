package com.eis.networklibrary.kademlia;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.eis.networklibrary.kademlia.SMSNetworkManager.ReplyType;
import com.eis.networklibrary.kademlia.SMSNetworkManager.RequestType;
import com.eis.smslibrary.SMSHandler;
import com.eis.smslibrary.SMSMessage;
import com.eis.smslibrary.SMSPeer;
import com.eis.smslibrary.listeners.SMSSentListener;

/**
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
 * @author Luca Crema, Marco Mariotto
 * @since 10/12/2019
 */
@SuppressWarnings({"WeakerAccess", "unused"})
class SMSCommandMapper {

    final static String SPLIT_CHAR = "-";
    private static SMSHandler handler = SMSHandler.getInstance();

    /**
     * Sends a sms containing the request and its content.
     *
     * @param req          the request type.
     * @param content      the data of the request.
     * @param peer         the recipient of the request.
     * @param sentListener callback for when the message is sent.
     */
    public static void sendRequest(final @NonNull RequestType req, final @NonNull String content, final @NonNull SMSPeer peer, final @Nullable SMSSentListener sentListener) {
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
    public static void sendRequest(final @NonNull RequestType req, final @NonNull String content, final @NonNull SMSPeer peer) {
        sendRequest(req, content, peer, null);
    }

    /**
     * Sends an sms with the request. Useful for those requests that don't need content.
     *
     * @param req  the request type.
     * @param peer the recipient of the request.
     */
    public static void sendRequest(final @NonNull RequestType req, final @NonNull SMSPeer peer, final @NonNull SMSSentListener sentListener) {
        sendRequest(req, "", peer, sentListener);
    }

    /**
     * Sends an sms with the request. Useful for those requests that don't need content.
     *
     * @param req  the request type.
     * @param peer the recipient of the request.
     */
    public static void sendRequest(final @NonNull RequestType req, final @NonNull SMSPeer peer) {
        sendRequest(req, "", peer, null);
    }

    /**
     * Sends an sms with the reply.
     *
     * @param reply        the reply type.
     * @param content      the data of the reply.
     * @param peer         the recipient of the reply.
     * @param sentListener callback for when message is sent.
     */
    public static void sendReply(final @NonNull ReplyType reply, final @NonNull String content, final @NonNull SMSPeer peer, final @Nullable SMSSentListener sentListener) {
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
    public static void sendReply(final @NonNull ReplyType reply, final @NonNull String content, final @NonNull SMSPeer peer) {
        sendReply(reply, content, peer, null);
    }

    /**
     * Sends an sms with the reply. Useful for those replies that don't need content.
     *
     * @param reply        the reply type.
     * @param peer         the recipient of the reply.
     * @param sentListener callback for when message is sent.
     */
    public static void sendReply(final @NonNull ReplyType reply, final @NonNull SMSPeer peer, final @NonNull SMSSentListener sentListener) {
        sendReply(reply, "", peer, sentListener);
    }

    /**
     * Sends an sms with the reply. Useful for those replies that don't need content.
     *
     * @param reply the reply type.
     * @param peer  the recipient of the reply.
     */
    public static void sendReply(final @NonNull ReplyType reply, final @NonNull SMSPeer peer) {
        sendReply(reply, "", peer, null);
    }

    /**
     * Parses a request into a string to put into a message.
     *
     * @param req     The request.
     * @param content The content of the request. Can be empty
     * @return The request parsed into a string ready to be sent.
     */
    private static String buildRequest(final @NonNull RequestType req, final @NonNull String content) {
        return content.isEmpty() ? req.toString() : (req.toString() + SPLIT_CHAR + content);
    }

    /**
     * Parses a reply into a string to put into a message.
     *
     * @param reply   The reply.
     * @param content The content of the reply. Can be empty
     * @return The reply parsed into a string ready to be sent.
     */
    private static String buildReply(final @NonNull ReplyType reply, final @NonNull String content) {
        return content.isEmpty() ? reply.toString() : (reply.toString() + SPLIT_CHAR + content);
    }
}

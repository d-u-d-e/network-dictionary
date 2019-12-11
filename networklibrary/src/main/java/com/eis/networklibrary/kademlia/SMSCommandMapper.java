package com.eis.networklibrary.kademlia;

import androidx.annotation.NonNull;

import com.eis.networklibrary.kademlia.SMSNetworkManager.Reply;
import com.eis.networklibrary.kademlia.SMSNetworkManager.Request;
import com.eis.smslibrary.SMSHandler;
import com.eis.smslibrary.SMSMessage;
import com.eis.smslibrary.SMSPeer;
import com.eis.smslibrary.listeners.SMSSentListener;

/**
 * @author Luca Crema
 * @author Marco Mariotto
 * <p>
 * /**
 * * SPLIT_CHAR = '_' is used to split fields in each request or reply
 * * <p>
 * * SMS REQUESTS FORMATS
 * * JOIN proposal:      "JOIN_REQUEST%netName"            netName is the name of the network the new node is asked to join
 * * PING request:       "PI_%(randomId)"         randomId is an identifier to match ping requests with replies
 * * STORE request:      "ST_%(key)_%(value)"
 * * FIND_NODE request:  "FN_%(KADAddress)"          find the K-CLOSEST nodes to this KAD peer (we want to know their phone numbers)
 * * FIND_VALUE request: "FV_%(key)
 * * <p>
 * * <p>
 * * SMS REPLIES FORMATS
 * * JOIN agreed:       "PJ_%netName" //we use the same notation to keep it consistent with NF and VF
 * * PING reply:        "IP_%(matchingId)"
 * * NODE_FOUND reply:  "NF_%(phoneNumber)_%(KADAddress)"  TODO how many entries should we pack inside this reply?
 * * VALUE_FOUND reply: "VF_%(key)_(value)" TODO should send also key to match with value? Or use a randomId like in PING?
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
    public static void sendRequest(@NonNull final Request req, @NonNull final String content, @NonNull final SMSPeer peer, @NonNull final SMSSentListener sentListener) {
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
    public static void sendRequest(@NonNull final Request req, @NonNull final String content, @NonNull final SMSPeer peer) {
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
    public static void sendReply(@NonNull final Reply reply, @NonNull final String content, @NonNull final SMSPeer peer, @NonNull final SMSSentListener sentListener) {
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
    public static void sendReply(@NonNull final Reply reply, @NonNull final String content, @NonNull final SMSPeer peer) {
        sendReply(reply, content, peer, null);
    }

    /**
     * Sends an sms with the reply. Useful for those replies that don't need content.
     *
     * @param reply        the reply type.
     * @param peer         the recipient of the reply.
     * @param sentListener callback for when message is sent.
     */
    public static void sendReply(@NonNull final Reply reply, @NonNull final SMSPeer peer, @NonNull final SMSSentListener sentListener) {
        SMSMessage messageReply = new SMSMessage(peer, buildReply(reply));
        handler.sendMessage(messageReply, sentListener);
    }

    /**
     * Sends an sms with the reply. Useful for those replies that don't need content.
     *
     * @param reply the reply type.
     * @param peer  the recipient of the reply.
     */
    public static void sendReply(@NonNull final Reply reply, @NonNull final SMSPeer peer) {
        sendReply(reply, peer, null);
    }

    /**
     * Parses a request into a string to put into a message.
     *
     * @param req     The request.
     * @param content The content of the request.
     * @return The request parsed into a string ready to be sent.
     */
    private static String buildRequest(@NonNull final Request req, @NonNull final String content) {
        return req.toString() + SPLIT_CHAR + content;
    }

    /**
     * Parses a reply into a string to put into a message.
     *
     * @param reply   The reply.
     * @param content The content of the reply.
     * @return The reply parsed into a string ready to be sent. Can't be null, if you don't have content use {@link #buildReply(Reply)}
     */
    private static String buildReply(@NonNull final Reply reply, @NonNull final String content) {
        return reply.toString() + SPLIT_CHAR + content;
    }

    /**
     * Parses a reply into a string to put into a message.
     * This method is for those replies that don't use the content.
     *
     * @param reply The reply.
     * @return The reply parsed into a string ready to be sent.
     */
    private static String buildReply(@NonNull final Reply reply) {
        return reply.toString();
    }

    /**
     * Method called by {@link SMSNetworkListener} when a request command is received.
     *
     * @param req            The command received.
     * @param commandContent The content of the command without the command prefix, can be empty.
     */
    protected static void processRequest(@NonNull final Request req, @NonNull final SMSPeer peer, @NonNull final String commandContent) {
        switch (req) {
            case JOIN_PROPOSAL:
                SMSNetworkManager.getInstance().onJoinProposal(peer);
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
     * Method called by {@link SMSNetworkListener} when a reply command is received.
     *
     * @param reply          The command received.
     * @param commandContent The content of the command without the command prefix, can be empty.
     */
    protected static void processReply(@NonNull final Reply reply, @NonNull final SMSPeer peer, @NonNull final String commandContent) {
        switch (reply) {
            case JOIN_AGREED:
                SMSNetworkManager.getInstance().onJoinReply(peer);
                break;
            case PING_ECHO:
                SMSNetworkManager.getInstance().onPingReply(peer);
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

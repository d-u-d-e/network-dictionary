//package com.eis.networklibrary.kademlia;
//
//
//import java.util.Map;
//
///**
// * This class handles the Request and Result listeners
// *
// * @author Marco Tommasini
// */
//public class SMSNetworkListenerHandler {
//
//    private Map<SMSNetworkManager.RequestType, ConverseListener> waitingRequestListeners;
//    private Map<SMSNetworkManager.ReplyType, ConverseListener> waitingReplyListeners;
//
//    /**
//     * Adds the request and listener to the waiting list
//     *
//     * @param request
//     * @param listener
//     */
//    protected void setRequestListener(SMSNetworkManager.RequestType request, ConverseListener listener) {
//        waitingRequestListeners.put(request, listener);
//    }
//
//    /**
//     * Adds the reply and listener to the waiting list
//     *
//     * @param reply
//     * @param listener
//     */
//    protected void setReplyListener(SMSNetworkManager.ReplyType reply, ConverseListener listener) {
//        waitingReplyListeners.put(reply, listener);
//    }
//
//}

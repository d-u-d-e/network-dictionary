package com.eis.networklibrary.kademlia;


import java.util.Map;

/**
 * This class handles the Request and Result listeners
 *
 * @author Marco Tommasini
 */
public class SMSNetworkListenerHandler {

    private Map<SMSNetworkManager.Request, ConverseListener> waitingRequestListeners
    private Map<SMSNetworkManager.Reply, ConverseListener> waitingReplyListeners;

    /**
     * Adds the request and listener to the waiting list
     *
     * @param request
     * @param listener
     */
    protected void setRequestListener(SMSNetworkManager.Request request, ConverseListener listener) {
        waitingRequestListeners.put(request, listener);
    }

    /**
     * Adds the reply and listener to the waiting list
     *
     * @param reply
     * @param listener
     */
    protected void setReplyListener(SMSNetworkManager.Reply reply, ConverseListener listener) {
        waitingReplyListeners.put(reply, listener);
    }

}

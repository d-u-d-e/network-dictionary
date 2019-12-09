package com.eis.networklibrary.distributed;

import com.eis.smslibrary.SMSMessage;
import com.eis.smslibrary.listeners.SMSReceivedServiceListener;

import java.util.Arrays;

/**
 *
 * This listener receives messages from the broadcast receiver and looks for messages forwarded by
 * the network. It is abstract since an actual implementation requires an instance of SMSNetworkManager,
 * which is abstract (see the class for further explanation).
 *
 * @author Marco Mariotto, Alessandra Tonin
 */
public abstract class SMSAbstractNetworkListener extends SMSReceivedServiceListener
{

    /**
     * This listener needs an instance of manager in order to let it process incoming requests.
     * JOIN_PROPOSAL requests are handled by the application overriding onJoinProposal().
     * Other requests or replies are handled by the manager.
     * When we will deal with multiple networks this listener will need a manager for each network.
     */
    protected SMSAbstractNetworkManager manager;

    /**
     * SPLIT_CHAR = '_' is used to split fields in each request or reply
     *
     * SMS REQUESTS FORMATS
     * JOIN proposal:      "JP_%netName"            netName is the name of the network the new node is asked to join
     * PING request:       "PI_%(randomId)"         randomId is an identifier to match ping requests with replies
     * STORE request:      "ST_%(key)_%(value)"
     * FIND_NODE request:  "FN_%(KADPeer)"          find the K-CLOSEST nodes to this KAD peer (we want to know their phone numbers)
     * FIND_VALUE request: "FV_%(key)
     *
     *
     * SMS REPLIES FORMATS
     * JOIN agreed:       "PJ_%netName" //we use the same notation to keep it consistent with NF and VF
     * PING reply:        "IP_%(matchingId)"
     * NODE_FOUND reply:  "NF_%(phoneNumber)_%(KADPeer)"  TODO how many entries should we pack inside this reply?
     * VALUE_FOUND reply: "VF_%(key)_(value)" TODO should send also key to mach with value? Or use a randomId like in PING?
     * */

    protected static final SMSAbstractNetworkManager.Reply[] REPLIES = {SMSAbstractNetworkManager.Reply.PING_ECHO, SMSAbstractNetworkManager.Reply.NODE_FOUND,
            SMSAbstractNetworkManager.Reply.VALUE_FOUND, SMSAbstractNetworkManager.Reply.JOIN_AGREED};
    protected static final SMSAbstractNetworkManager.Request[] REQUESTS = {SMSAbstractNetworkManager.Request.JOIN_PROPOSAL, SMSAbstractNetworkManager.Request.PING,
            SMSAbstractNetworkManager.Request.STORE, SMSAbstractNetworkManager.Request.FIND_NODE, SMSAbstractNetworkManager.Request.FIND_VALUE};
    @Override
    public void onMessageReceived(SMSMessage message){

        /*TODO: solve String problem
         * Is it better to use constants instead of enum, or to use toString() or similar methods to convert objects??
         */
        String command = message.getData().split(SMSAbstractNetworkManager.SPLIT_CHAR)[0];
        if ((!Arrays.asList(REQUESTS).contains(command)) && (!Arrays.asList(REPLIES).contains(command))) {
            throw new IllegalArgumentException("Unknown command received");
        } else if (command.equals(SMSAbstractNetworkManager.Request.JOIN_PROPOSAL.toString()))
            onJoinProposal(message);
        else {
            if (manager == null)
                throw new IllegalStateException("Message not expected: a manager has not been assigned for this network message");
            manager.processMessage(message);
        }

    }

    /**
     * This method is for handling a JoinProposal. It is up to the application to override it.
     *
     * @param message as SMSMessage
     */
    public abstract void onJoinProposal(SMSMessage message);

}
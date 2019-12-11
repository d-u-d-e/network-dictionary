package com.eis.networklibrary.kademlia;

import com.eis.smslibrary.SMSMessage;
import com.eis.smslibrary.listeners.SMSReceivedServiceListener;

import java.util.Arrays;

/**
 * This listener receives messages from the broadcast receiver and looks for messages forwarded by
 * the network. It is abstract since an actual implementation requires an instance of SMSNetworkManager,
 * which is abstract (see the class for further explanation).
 *
 * @author Marco Mariotto
 * @author Alessandra Tonin
 */
public abstract class SMSAbstractNetworkListener extends SMSReceivedServiceListener {

    /*
     * This listener needs an instance of manager in order to let it process incoming requests.
     * JOIN_PROPOSAL requests are handled by the application overriding onJoinProposal().
     * Other requests or replies are handled by the manager.
     * When we will deal with multiple networks this listener will need a manager for each network.
     */
    protected SMSAbstractNetworkManager manager;

    /**
     * SPLIT_CHAR = '_' is used to split fields in each request or reply
     * <p>
     * SMS REQUESTS FORMATS
     * JOIN proposal:      "JP_%netName"            netName is the name of the network the new node is asked to join
     * PING request:       "PI_%(randomId)"         randomId is an identifier to match ping requests with replies
     * STORE request:      "ST_%(key)_%(value)"
     * FIND_NODE request:  "FN_%(KADAddress)"          find the K-CLOSEST nodes to this KAD peer (we want to know their phone numbers)
     * FIND_VALUE request: "FV_%(key)
     * <p>
     * <p>
     * SMS REPLIES FORMATS
     * JOIN agreed:       "PJ_%netName" //we use the same notation to keep it consistent with NF and VF
     * PING reply:        "IP_%(matchingId)"
     * NODE_FOUND reply:  "NF_%(phoneNumber)_%(KADAddress)"  TODO how many entries should we pack inside this reply?
     * VALUE_FOUND reply: "VF_%(key)_(value)" TODO should send also key to match with value? Or use a randomId like in PING?
     */

    @Override
    public void onMessageReceived(SMSMessage message) {
        String command = message.getData().split(SMSAbstractNetworkManager.SPLIT_CHAR)[0];
        if ((!Arrays.asList(SMSAbstractNetworkManager.REQUESTS).contains(command)) && (!Arrays.asList(SMSAbstractNetworkManager.REPLIES).contains(command))) {
            throw new IllegalArgumentException("Unknown command received");
        }
        else {
            if (manager == null)
                throw new IllegalStateException("Message not expected: a manager has not been assigned for this network message");
            SMSCommandMapper.processMessage(message);
        }

    }

}
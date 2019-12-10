package com.eis.networklibrary.kademlia;

import com.eis.networklibrary.kademlia.SMSAbstractNetworkManager.Reply;
import com.eis.networklibrary.kademlia.SMSAbstractNetworkManager.Request;

/**
 * Maps command in SMSs and SMSs in commands.
 * <p>
 * SPLIT_CHAR = '_' is used to split fields in each request or reply
 * <p>
 * SMS REQUESTS FORMATS
 * JOIN proposal:      "JP_%netName"         netName is the name of the network the new node is asked to join
 * PING request:       "PI_%(randomId)"      randomId is an identifier to match ping requests with replies
 * STORE request:      "ST_%(key)_%(value)"
 * FIND_NODE request:  "FN_%(KADAddress)"    find the K-CLOSEST nodes to this KAD peer (we want to know their phone numbers)
 * FIND_VALUE request: "FV_%(key)
 * <p>
 * <p>
 * SMS REPLIES FORMATS
 * JOIN agreed:       "PJ_%netName" //we use the same notation to keep it consistent with NF and VF
 * PING reply:        "IP_%(matchingId)"
 * NODE_FOUND reply:  "NF_%(phoneNumber)_%(KADAddress)"  TODO how many entries should we pack inside this reply?
 * VALUE_FOUND reply: "VF_%(key)_(value)" TODO should send also key to mach with value? Or use a randomId like in PING?
 *
 * @author Marco Mariotto
 * @author Luca Crema
 * @since 10/12/2019
 */
class SMSCommandMapper {

    final static String SPLIT_CHAR = "_";

    public static String buildRequest(Request req, String... args) {
        String requestStr = "";
        switch (req) {
            case JOIN_PROPOSAL:
                requestStr = "JP_%s";
                break;
            case PING:
                requestStr = "PI_%s";
                break;
            case FIND_NODE:
                requestStr = "FN_%s";
                break;
            case FIND_VALUE:
                requestStr = "FV_%s";
                break;
            case STORE:
                requestStr = "ST_%s_%s";
                break;
        }
        return String.format(requestStr, args);
    }

    private Reply parseReply(String messageData) {
        String command = messageData.split(SMSAbstractNetworkManager.SPLIT_CHAR)[0];
        return Reply.JOIN_AGREED;
    }

}

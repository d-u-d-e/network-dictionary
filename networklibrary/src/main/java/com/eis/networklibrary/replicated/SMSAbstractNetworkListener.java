package com.eis.networklibrary.replicated;

import com.eis.smslibrary.SMSMessage;
import com.eis.smslibrary.listeners.SMSReceivedServiceListener;

import java.util.Arrays;

/**
 * This listener receives messages from the broadcast receiver and looks for messages forwarded by
 * the network. It is abstract since an actual implementation requires an instance of SMSNetworkManager,
 * which is abstract (see the class for further explanation).
 *
 * @author Marco Mariotto, Alessandra Tonin
 */
public abstract class SMSAbstractNetworkListener extends SMSReceivedServiceListener {

    /**
     * This listener needs an instance of manager in order to let it process incoming requests.
     * JOIN_PROPOSAL requests are handled by the application overriding onJoinProposal().
     * Other requests are handled by the manager, such as adding a user.
     * When we will deal with multiple networks this listener will need a manager for each network.
     */
    protected SMSAbstractNetworkManager manager;

    /**
     * SMS REQUESTS FORMATS
     * Join proposal:    "JP_%netName"
     * Add user:         "AU_%(requesterIndex)_%(peer)"          we include the whole peer, not only his address
     * Remove user:      "RU_%(requesterIndex)_%(address)"       address is the phone number of the user being removed
     * Add resource:     "AR_%(requesterIndex)_%(key)_%(value)"  we include the whole resource, key and value
     * Remove resource:  "RR_%(requesterIndex)_%(key)"           we only need the key to identify a resource
     * Don't spread:     "%(1)DS_%(2)"                           inform the receiver to not spread this info, %(1) is one of {AU, RU, AR, RR},
     *                                                           %(2) can be peer, address, a <key, value> pair or key
     */
    protected static final String[] REQUESTS = {SMSAbstractNetworkManager.ADD_USER, SMSAbstractNetworkManager.REMOVE_USER,
            SMSAbstractNetworkManager.ADD_RESOURCE, SMSAbstractNetworkManager.REMOVE_RESOURCE, SMSAbstractNetworkManager.DO_NOT_SPREAD,
            SMSAbstractNetworkManager.JOIN_AGREED, SMSAbstractNetworkManager.JOIN_PROPOSAL};

    @Override
    public void onMessageReceived(SMSMessage message) {
        String request = message.getData().split(SMSAbstractNetworkManager.SPLIT_CHAR)[0];
        if (!Arrays.asList(REQUESTS).contains(request)) {
            throw new IllegalArgumentException("Unknown request received");
        } else if (request.equals(SMSAbstractNetworkManager.JOIN_PROPOSAL))
            onJoinProposal(message);
        else {
            if (manager == null)
                throw new IllegalStateException("Message not expected: a manager has not been assigned for this network message");
            manager.processRequest(message);
        }
    }

    /**
     * This method is for handling a JoinProposal. It is up to the application to override it.
     *
     * @param message as SMSMessage
     */
    public abstract void onJoinProposal(SMSMessage message);
}


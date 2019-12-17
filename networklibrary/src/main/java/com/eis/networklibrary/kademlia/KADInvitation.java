package com.eis.networklibrary.kademlia;

import com.eis.communication.network.Invitation;

/**
 * Represents an invitation to join a network of KAD
 */
public class KADInvitation implements Invitation<SMSKADPeer> {

    private SMSKADPeer inviter;
    private String networkName;

    /**
     * Constructor
     *
     * @param inviter     who invited you to join
     * @param networkName the name of the network you're invited to
     */
    public KADInvitation(SMSKADPeer inviter, String networkName) {
        this.inviter = inviter;
        this.networkName = networkName;
    }

    /**
     * @return the Peer that invited you to the network
     */
    @Override
    public SMSKADPeer getInviter() {
        return inviter;
    }

    /**
     * @return the String identifier of the network this invitation is for
     */
    public String getNetworkName() {
        return networkName;
    }
}

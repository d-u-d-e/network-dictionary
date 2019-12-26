package com.eis.networklibrary.kademlia;

import com.eis.communication.network.Invitation;

/**
 * Represents an invitation to join a network of KAD
 * May contain other useful information, such as the time when it was created
 * @author Marco Mariotto, Luca Crema
 */
public class KADInvitation extends Invitation<SMSKADPeer> {

     private String networkName;
    /**
     * //package private constructor
     *
     * @param inviter     who asks guest to join
     * @param guest       who is asked to join
     * @param networkName the name of the network guest is asked to join
     */
    KADInvitation(SMSKADPeer inviter, SMSKADPeer guest, String networkName) {
        this.inviter = inviter;
        this.guest = guest;
        this.networkName = networkName;
    }

    SMSKADPeer getInviter() {return (SMSKADPeer) inviter;}
    SMSKADPeer getGuest() {return  (SMSKADPeer) guest;}
    String getNetworkName() {return networkName;}
}

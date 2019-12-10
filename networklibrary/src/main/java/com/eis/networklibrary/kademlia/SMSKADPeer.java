package com.eis.networklibrary.kademlia;

import com.eis.communication.network.kademlia.KADPeer;
import com.eis.smslibrary.SMSPeer;
import com.eis.smslibrary.exceptions.InvalidTelephoneNumberException;

public class SMSKADPeer extends SMSPeer implements KADPeer {
    protected KADAddress networkAddress;

    /**
     * @param telephoneNumber Address for the peer.
     * @throws InvalidTelephoneNumberException If telephoneNumber check is not {@link TelephoneNumberState#TELEPHONE_NUMBER_VALID}.
     */
    public SMSKADPeer(String telephoneNumber) throws InvalidTelephoneNumberException {
        super(telephoneNumber);
        networkAddress = new KADAddress(telephoneNumber);
    }

    public SMSKADPeer(SMSPeer smsPeer) {
        this(smsPeer.getAddress());
    }

    @Override
    public KADAddress getNetworkAddress() {
        return networkAddress;
    }

}

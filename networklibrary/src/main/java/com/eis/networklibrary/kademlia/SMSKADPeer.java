package com.eis.networklibrary.kademlia;

import com.eis.communication.network.kademlia.KADPeer;
import com.eis.smslibrary.SMSPeer;
import com.eis.smslibrary.exceptions.InvalidTelephoneNumberException;

/**
 * //TODO
 */
public class SMSKADPeer extends SMSPeer implements KADPeer {

    protected KADAddress networkAddress;

    /**
     * TODO
     *
     * @param telephoneNumber Address for the peer.
     * @throws InvalidTelephoneNumberException If telephoneNumber check is not {@link TelephoneNumberState#TELEPHONE_NUMBER_VALID}.
     */
    public SMSKADPeer(String telephoneNumber) throws InvalidTelephoneNumberException {
        super(telephoneNumber);
        networkAddress = new KADAddress(telephoneNumber);
    }

    /**
     * TODO
     *
     * @param smsPeer TODO
     */
    public SMSKADPeer(SMSPeer smsPeer) {
        this(smsPeer.getAddress());
    }

    /**
     * TODO
     *
     * @return the peer's network address
     */
    @Override
    public KADAddress getNetworkAddress() {
        return networkAddress;
    }

}

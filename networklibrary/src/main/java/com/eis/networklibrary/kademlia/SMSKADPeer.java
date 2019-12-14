package com.eis.networklibrary.kademlia;

import com.eis.communication.network.kademlia.KADPeer;
import com.eis.smslibrary.SMSPeer;
import com.eis.smslibrary.exceptions.InvalidTelephoneNumberException;

import java.util.Comparator;

public class SMSKADPeer extends SMSPeer implements KADPeer {

    protected KADAddress networkAddress;

    /**
     * constructs a {@code SMSKADPeer} object from its phone number
     *
     * @param telephoneNumber phone number of this peer.
     * @throws InvalidTelephoneNumberException if telephoneNumber check does not return {@link TelephoneNumberState#TELEPHONE_NUMBER_VALID}.
     */
    public SMSKADPeer(String telephoneNumber) throws InvalidTelephoneNumberException {
        super(telephoneNumber);
        networkAddress = new KADAddress(telephoneNumber);
    }

    /**
     * constructs a {@code SMSKADPeer} object from a {@link SMSPeer}
     * @param smsPeer from which a SMSKADPeer is built
     */
    public SMSKADPeer(SMSPeer smsPeer) {
        this(smsPeer.getAddress());
    }

    /**
     * @return the peer's network address
     */
    @Override
    public KADAddress getNetworkAddress() {
        return networkAddress;
    }


    /**
    * a static nested class defining a comparator for KAD addresses. A target must be specified in order to compare addresses to it.
    */
    public static class KADComparator implements Comparator<SMSKADPeer> {
        KADAddress target;

        /**
         * @param target used to compare addresses to it
         */

        KADComparator(KADAddress target) {
            this.target = target;
        }

        @Override
        public int compare(SMSKADPeer o1, SMSKADPeer o2) {
            KADAddress a1 = o1.getNetworkAddress();
            KADAddress a2 = o2.getNetworkAddress();
            if (a1.equals(a2)) return 0;
            KADAddress closer = KADAddress.closerToTarget(a1, a2, target);
            if (closer.equals(a1)) return -1;
            else return 1;
        }
    }
}

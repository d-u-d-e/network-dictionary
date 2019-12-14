package com.eis.networklibrary.kademlia;

import com.eis.communication.network.kademlia.KADPeer;
import com.eis.smslibrary.SMSPeer;
import com.eis.smslibrary.exceptions.InvalidTelephoneNumberException;

import java.util.Comparator;

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


    public static class KADComparator implements Comparator<SMSKADPeer> {
        KADAddress target;

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

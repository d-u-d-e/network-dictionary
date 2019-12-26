package com.eis.networklibrary.kademlia;

import com.eis.smslibrary.SMSPeer;
import com.eis.smslibrary.exceptions.InvalidTelephoneNumberException;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

/**
 * @author ALberto Ursino
 */
public class SMSKADPeerTest {

    private final String PHONE_NUMBER = "+391111111111";
    private final String PHONE_NUMBER2 = "+391111111112";
    private String WRONG_PHONE_NUMBER = "ciao";
    private String EXCEPTION_EXPECTED_MESSAGE = "Should have thrown an exception";
    private int COMPARE_NEG_INT = -1;
    private int COMPARE_POS_INT = 1;
    private int COMPARE_ZERO_INT = 0;

    private SMSPeer smsPeer = new SMSPeer(PHONE_NUMBER);
    private KADAddress kadAddress = new KADAddress(new byte[]{106, 97, 118, 97, 32, 105, 115, 32, 111, 107});
    private KADAddress kadAddress2 = new KADAddress(new byte[]{107, 97, 118, 97, 32, 105, 115, 32, 111, 107});
    private KADAddress target = new KADAddress(new byte[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1});

    private SMSKADPeer.SMSKADComparator kadComparator;
    private SMSKADPeer smsPeerKadPeer, stringKadPeer, string2KadPeer;

    @Before
    public void init() {
        smsPeerKadPeer = new SMSKADPeer(smsPeer);
        stringKadPeer = new SMSKADPeer(PHONE_NUMBER);
        string2KadPeer = new SMSKADPeer(PHONE_NUMBER2);
        kadComparator = new SMSKADPeer.SMSKADComparator(target);
    }

    @Test
    public void getNetworkAddress_kadAddresses_test() {
        smsPeerKadPeer.networkAddress = kadAddress;
        assertEquals(kadAddress, smsPeerKadPeer.getNetworkAddress());
        assertNotEquals(kadAddress2, smsPeerKadPeer.getNetworkAddress());
    }

    @Test
    public void compare_smsKadPeer_test() {
        //Equals
        assertEquals(COMPARE_ZERO_INT, kadComparator.compare(stringKadPeer, stringKadPeer));
        //First is closer
        assertEquals(COMPARE_NEG_INT, kadComparator.compare(stringKadPeer, string2KadPeer));
        //Second is closer
        assertEquals(COMPARE_POS_INT, kadComparator.compare(string2KadPeer, stringKadPeer));
    }

    @Test(expected = InvalidTelephoneNumberException.class)
    public void constructor_phoneNumber_invalid() {
        try {
            new SMSKADPeer(WRONG_PHONE_NUMBER);
            fail(EXCEPTION_EXPECTED_MESSAGE);
        } catch (InvalidTelephoneNumberException e) {
            //Success
        }
    }

}
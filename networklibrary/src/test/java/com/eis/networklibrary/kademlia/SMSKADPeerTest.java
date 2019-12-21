package com.eis.networklibrary.kademlia;

import com.eis.smslibrary.SMSPeer;
import com.eis.smslibrary.exceptions.InvalidTelephoneNumberException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author ALberto Ursino
 */
public class SMSKADPeerTest {

    final String PHONE_NUMBER = "+391111111111";
    final String PHONE_NUMBER2 = "+391111111112";
    final String WRONG_PHONE_NUMBER = "ciao";
    final String ASSERT_FAIL_EXCEPTION_MESSAGE = "Should have thrown an exception";
    final int COMPARE_NEG_INT = -1;
    final int COMPARE_POS_INT = 1;
    final int COMPARE_ZERO_INT = 0;

    SMSPeer smsPeer = new SMSPeer(PHONE_NUMBER);
    KADAddress kadAddress = new KADAddress(new byte[]{106, 97, 118, 97, 32, 105, 115, 32, 111, 107});
    KADAddress kadAddress2 = new KADAddress(new byte[]{107, 97, 118, 97, 32, 105, 115, 32, 111, 107});
    KADAddress target = new KADAddress(new byte[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1});

    SMSKADPeer.SMSKADComparator kadComparator;
    SMSKADPeer smsPeerKadPeer, stringKadPeer, string2KadPeer;

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
        Assert.assertEquals(kadAddress, smsPeerKadPeer.getNetworkAddress());
        Assert.assertNotEquals(kadAddress2, smsPeerKadPeer.getNetworkAddress());
    }

    @Test
    public void compare_smsKadPeer_test() {
        //Equals
        Assert.assertEquals(COMPARE_ZERO_INT, kadComparator.compare(stringKadPeer, stringKadPeer));
        //First is closer
        Assert.assertEquals(COMPARE_NEG_INT, kadComparator.compare(stringKadPeer, string2KadPeer));
        //Second is closer
        Assert.assertEquals(COMPARE_POS_INT, kadComparator.compare(string2KadPeer, stringKadPeer));
    }

    @Test
    public void constructor_phoneNumber_invalid() {
        try {
            new SMSKADPeer(WRONG_PHONE_NUMBER);
            Assert.fail(ASSERT_FAIL_EXCEPTION_MESSAGE);
        } catch (InvalidTelephoneNumberException e) {
            //Success
        }
    }

}
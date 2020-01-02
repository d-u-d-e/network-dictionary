package com.eis.networklibrary.kademlia;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Alessandra Tonin
 */
public class KADInvitationTest {

    private static final String NETWORK_NAME = "test_network";
    private KADInvitation testInvitation;
    private static final SMSKADPeer DEFAULT_INVITER = new SMSKADPeer("+393457090735");
    private static final SMSKADPeer DEFAULT_GUEST = new SMSKADPeer("+393884726001");

    @Before
    public void setup() {
        testInvitation = new KADInvitation(DEFAULT_INVITER, DEFAULT_GUEST, NETWORK_NAME);

    }

    @Test
    public void getInviterTest() {
        Assert.assertEquals(DEFAULT_INVITER, testInvitation.getInviter());
    }

    @Test
    public void getGuestTest() {
        Assert.assertEquals(DEFAULT_GUEST, testInvitation.getGuest());
    }

    @Test
    public void getNetworkNameTest() {
        Assert.assertEquals(NETWORK_NAME, testInvitation.getNetworkName());

    }
}
package com.eis.networklibrary.kademlia;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

public class ClosestPQTest {

    private ClosestPQ closestPQ;
    private static KADAddress TARGET = new KADAddress("+393457090735");
    private static final SMSKADPeer.SMSKADComparator DEFAULT_COMPARATOR = new SMSKADPeer.SMSKADComparator(TARGET);
    private final ArrayList<SMSKADPeer> knownNodes = new ArrayList<>();
    private static final ArrayList<MutablePair<SMSKADPeer, Boolean>> DEFAULT_PAIRS = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        closestPQ = new ClosestPQ(DEFAULT_COMPARATOR, knownNodes);
    }

    private void fillKnownNodes(){
        knownNodes.add(new SMSKADPeer("+393489685222"));
        knownNodes.add(new SMSKADPeer("+393589685772"));
        knownNodes.add(new SMSKADPeer("+393500685222"));
        knownNodes.add(new SMSKADPeer("+393589897522"));
    }

    @Test
    public void getTest() {
        SMSKADPeer peer = new SMSKADPeer("+393457090735");
        closestPQ.add(peer, false);
        Assert.assertEquals(peer, knownNodes.get(0));
    }

    @Test
    public void sizeTest() {
    }

    @Test
    public void getAllPeersTest() {
    }
}
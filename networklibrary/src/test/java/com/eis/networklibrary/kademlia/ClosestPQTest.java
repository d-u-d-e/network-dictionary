package com.eis.networklibrary.kademlia;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class ClosestPQTest {

    private ClosestPQ closestPQ;
    private static KADAddress TARGET = new KADAddress("+393457090735");
    private static final SMSKADPeer.SMSKADComparator DEFAULT_COMPARATOR = new SMSKADPeer.SMSKADComparator(TARGET);
    private final ArrayList<SMSKADPeer> knownNodes = new ArrayList<>();
    private static final ArrayList<MutablePair<SMSKADPeer, Boolean>> DEFAULT_PAIRS = new ArrayList<>();

    /**
     * Compares the elements of two arrays as a set (doesn't care about position)
     *
     * @param array1
     * @param array2
     * @return
     */
    private boolean compareArray(Object[] array1, Object[] array2) {
        HashSet<Object> set1 = new HashSet<>(Arrays.asList(array1));
        HashSet<Object> set2 = new HashSet<>(Arrays.asList(array2));
        return set1.equals(set2);
    }

    private boolean compareArray(Object[] array1, ArrayList array2) {
        HashSet<Object> set1 = new HashSet<>(Arrays.asList(array1));
        HashSet set2 = new HashSet<>(array2);
        return set1.equals(set2);
    }

    private boolean compareArray(ArrayList array1, ArrayList array2) {
        HashSet set1 = new HashSet<>(array1);
        HashSet set2 = new HashSet<>(array2);
        return set1.equals(set2);
    }

    @Before
    public void setUp() throws Exception {
        closestPQ = new ClosestPQ(DEFAULT_COMPARATOR, knownNodes);
    }

    private void fillKnownNodes(){
        knownNodes.add(new SMSKADPeer("+393489685222"));
        knownNodes.add(new SMSKADPeer("+393589685772"));
        knownNodes.add(new SMSKADPeer("+393500685222"));
        knownNodes.add(new SMSKADPeer("+393589897522"));
        closestPQ.add(new SMSKADPeer("+393489685222"), false);
        closestPQ.add(new SMSKADPeer("+393589685772"), false);
        closestPQ.add(new SMSKADPeer("+393500685222"), false);
        closestPQ.add(new SMSKADPeer("+393589897522"), false);
    }

    @Test
    public void getTest() {
        SMSKADPeer peer = new SMSKADPeer("+393457090735");
        closestPQ.add(peer, false);
        Assert.assertEquals(peer, closestPQ.get(0).first);
    }

    @Test
    public void sizeTest() {
    }

    @Test
    public void getAllPeersTest() {
        fillKnownNodes();
        Assert.assertTrue(compareArray(closestPQ.getAllPeers(), knownNodes));
    }
}
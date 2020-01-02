package com.eis.networklibrary.kademlia;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/**
 * @author Alessandra Tonin, Luca Crema
 */
public class ClosestPQTest {

    private ClosestPQ closestPQ;
    private static final KADAddress TARGET = new KADAddress("+393457090735");
    private final ArrayList<SMSKADPeer> knownNodes = new ArrayList<>();
    private static final ArrayList<MutablePair<SMSKADPeer, Boolean>> DEFAULT_PAIRS = new ArrayList<>();

    /**
     * Compares the elements of two arrays as a set (doesn't care about position)
     *
     * @param array1 First array in comparison
     * @param array2 Second array in comparison
     * @return true if the arrays contain same elements, false otherwise
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
    public void setUp() {
        closestPQ = new ClosestPQ(TARGET, knownNodes);
    }

    private void fill() {
        SMSKADPeer peer1 = new SMSKADPeer("+393489685222");
        SMSKADPeer peer2 = new SMSKADPeer("+393589685772");
        SMSKADPeer peer3 = new SMSKADPeer("+393500685222");
        SMSKADPeer peer4 = new SMSKADPeer("+393589897522");
        knownNodes.add(peer1);
        knownNodes.add(peer2);
        knownNodes.add(peer3);
        knownNodes.add(peer4);
        closestPQ.add(peer1, false);
        closestPQ.add(peer2, false);
        closestPQ.add(peer3, false);
        closestPQ.add(peer4, false);
        DEFAULT_PAIRS.add(new MutablePair<>(peer1, false));
        DEFAULT_PAIRS.add(new MutablePair<>(peer2, false));
        DEFAULT_PAIRS.add(new MutablePair<>(peer3, false));
        DEFAULT_PAIRS.add(new MutablePair<>(peer4, false));
    }

    @Test
    public void getTest() {
        SMSKADPeer peer = new SMSKADPeer("+393457090735");
        closestPQ.add(peer, false);
        Assert.assertEquals(peer, closestPQ.get(0).first);
    }

    @Test
    public void sizeTest() {
        fill();
        Assert.assertEquals(4, DEFAULT_PAIRS.size());
    }

    @Test
    public void getAllPeersTest() {
        fill();
        Assert.assertTrue(compareArray(closestPQ.getAllPeers(), knownNodes));
    }
}
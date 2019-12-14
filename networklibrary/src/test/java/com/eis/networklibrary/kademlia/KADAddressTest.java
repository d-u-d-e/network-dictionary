package com.eis.networklibrary.kademlia;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.BitSet;


public class KADAddressTest {

    final byte[] BYTE_ADDRESS = {1, 0, 1, 0, 0, 0, 0, 0, 0, 0};
    final String ADDRESS_STRING = "1010000000";
    final int BIT_SET_SIZE = 80;
    final BitSet BIT_SET_ADDRESS = new BitSet(BIT_SET_SIZE);
    final String BIT_SET_STRING = "-1-1-1-1-1-1-1-1-1-1";
    KADAddress kadAddress;

    @Before
    public void init() {
        for (int i = 0; i < BIT_SET_SIZE; i++) {
            BIT_SET_ADDRESS.set(i, true);
        }
    }

//    @Test
//    public void addressToString_byte_constructor() {
//        kadAddress = new KADAddress(BYTE_ADDRESS);
//        Assert.assertEquals(ADDRESS_STRING, kadAddress.addressToString());
//    }
//
//    @Test
//    public void addressToString_bitSet_constructor() {
//        kadAddress = new KADAddress(BIT_SET_ADDRESS);
//        Assert.assertEquals(BIT_SET_STRING, kadAddress.addressToString());
//    }
}
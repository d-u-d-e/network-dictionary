package com.eis.networklibrary.kademlia;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.BitSet;


public class KADAddressTest {

    final byte[] BYTE_ADDRESS = {106, 97, 118, 97, 32, 105, 115, 32, 111, 107};
    final String ADDRESS_STRING = "java is ok";
    final int BIT_SET_SIZE = 80;
    BitSet BIT_SET_ADDRESS;
    KADAddress kadAddress;

    @Before
    public void init() {
        for (int i = 0; i < BIT_SET_SIZE; i++) {
            BIT_SET_ADDRESS = BitSet.valueOf(BYTE_ADDRESS);
        }
    }

    @Test
    public void addressToString_byte_constructor() {
        kadAddress = new KADAddress(BYTE_ADDRESS);
        Assert.assertEquals(ADDRESS_STRING, kadAddress.toString());
    }

    @Test
    public void addressToString_bitSet_constructor() {
        kadAddress = new KADAddress(BIT_SET_ADDRESS);
        Assert.assertEquals(ADDRESS_STRING, kadAddress.toString());
    }

    @Test
    public void myTest(){
        BitSet set = new BitSet(80);
        System.out.println(set.size());
        set.set(0);
        set.set(7);
        System.out.println(set.get(127));
        byte[] arr = set.toByteArray();
        byte firstB = arr[0];
        int a = firstB & 0xFF;
        System.out.println(a);
    }
}
package com.eis.networklibrary.kademlia;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.BitSet;


public class KADAddressTest {

    final byte[] BYTE_ADDRESS = {106, 97, 118, 97, 32, 105, 115, 32, 111, 107};
    final String ADDRESS_STRING = "6A617661206973206F6B";
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
    public void toString_test(){
        kadAddress = new KADAddress(BYTE_ADDRESS);
        Assert.assertEquals(ADDRESS_STRING, kadAddress.toString());
    }

    @Test
    public void fromHexString_test() {
        kadAddress = new KADAddress(BYTE_ADDRESS);
        Assert.assertEquals(kadAddress, KADAddress.fromHexString(ADDRESS_STRING));
    }

    @Test
    public void myTest(){


    }
}
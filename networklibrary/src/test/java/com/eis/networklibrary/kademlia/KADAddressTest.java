
package com.eis.networklibrary.kademlia;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Marco Mariotto
 * @author Alberto Ursino
 */
public class KADAddressTest {

    final byte[] BYTE_ADDRESS = {106, 97, 118, 97, 32, 105, 115, 32, 111, 107};
    final byte[] BYTE_ADDRESS_2 = {106, 10, 118, 97, 32, 105, 115, 32, 111, 107};
    final String STRING_ADDRESS = "6A617661206973206F6B";
    final int FIRST_DIFFERENT_BIT_INDEX = 1;
    final int DEFAULT_VALUE_EQUAL_ADDRESSES = -1;
    KADAddress kadAddress;

    @Before
    public void init() {
        kadAddress = new KADAddress(BYTE_ADDRESS);
    }

    @Test
    public void toString_test() {
        Assert.assertEquals(STRING_ADDRESS, kadAddress.toString());
    }

    @Test
    public void fromHexString_test() {
        Assert.assertEquals(kadAddress, KADAddress.fromHexString(STRING_ADDRESS));
    }

    @Test
    public void getAddress_isOk() {
        Assert.assertEquals(kadAddress.address, kadAddress.getAddress());
    }

    @Test
    public void firstDifferentBit_address_areEquals() {
        Assert.assertEquals(DEFAULT_VALUE_EQUAL_ADDRESSES, kadAddress.firstDifferentBit(kadAddress));
    }

    @Test
    public void firstDifferentBit_addresses_areNotEquals() {
        Assert.assertNotEquals(DEFAULT_VALUE_EQUAL_ADDRESSES, kadAddress.firstDifferentBit(new KADAddress(BYTE_ADDRESS_2)));
    }

    @Test
    public void firstDifferentBit_return_firstDiffBit() {
        System.out.println(new KADAddress(BYTE_ADDRESS).toString());
        System.out.println(new KADAddress(BYTE_ADDRESS_2).toString());
        Assert.assertEquals(FIRST_DIFFERENT_BIT_INDEX, kadAddress.firstDifferentBit((new KADAddress(BYTE_ADDRESS_2))));
    }

    /*@Test
    public void generalTest() {

    }*/

}
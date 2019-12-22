
package com.eis.networklibrary.kademlia;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Marco Mariotto
 * @author Alberto Ursino
 */
public class KADAddressTest {

    KADAddress KAD_ADDRESS_1, KAD_ADDRESS_2, KAD_ADDRESS_3, KAD_ADDRESS_4, KAD_ADDRESS_5, KAD_ADDRESS_4_2;
    final String ADDRESS_1_TO_STRING = "6A617661206973206F6B";
    final String DEFAULT_STRING = "ciao";
    final String EMPTY_STRING = "";
    final String ASSERT_FAIL_EXCEPTION_MESSAGE = "Should have thrown an exception";
    final int FIRST_DIFF_BIT_INDEX_EXPECTED = 12;
    final byte[] TOO_SHORT_BYTE_ADDRESS = new byte[]{106, 97, 118};
    final byte[] TOO_LONG_BYTE_ADDRESS = new byte[]{106, 97, 118, 97, 32, 105, 115, 32, 111, 107, 0, 1, 2, 3, 4, 5};

    @Before
    public void init() {
        KAD_ADDRESS_1 = new KADAddress(new byte[]{106, 97, 118, 97, 32, 105, 115, 32, 111, 107});
        KAD_ADDRESS_2 = new KADAddress(new byte[]{12, 3, 118, 97, 32, 105, 115, 32, 111, 107});
        KAD_ADDRESS_3 = new KADAddress(new byte[]{12, 8, 118, 97, 32, 105, 115, 32, 111, 107});
        KAD_ADDRESS_4 = new KADAddress(new byte[]{106, 97, 118, 97, 32, 85, 115, 32, 111, 107});
        KAD_ADDRESS_4_2 = new KADAddress(new byte[]{106, 97, 118, 97, 32, 85, 115, 32, 111, 107});
        KAD_ADDRESS_5 = new KADAddress(new byte[]{106, 97, 118, 97, 32, 99, 115, 32, 111, 107});
    }

    @Test
    public void constructor_address_tooShort() {
        try {
            new KADAddress(TOO_SHORT_BYTE_ADDRESS);
            Assert.fail(ASSERT_FAIL_EXCEPTION_MESSAGE);
        } catch (IllegalArgumentException e) {
            //Success
        }
    }

    @Test
    public void constructor_address_tooLong() {
        try {
            new KADAddress(TOO_LONG_BYTE_ADDRESS);
            Assert.fail(ASSERT_FAIL_EXCEPTION_MESSAGE);
        } catch (IllegalArgumentException e) {
            //Success
        }
    }

    @Test
    public void constructor_string_isEmpty() {
        try {
            new KADAddress(EMPTY_STRING);
            //Success
        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test
    public void constructor_string_isOk() {
        try {
            new KADAddress(DEFAULT_STRING);
            //Success
        } catch (Exception e) {
            Assert.fail();
        }
    }


    @Test
    public void toString_isOk() {
        Assert.assertEquals(ADDRESS_1_TO_STRING, KAD_ADDRESS_1.toString());
    }

    @Test
    public void fromHexString_isOk() {
        Assert.assertEquals(KAD_ADDRESS_1, KADAddress.fromHexString(ADDRESS_1_TO_STRING));
    }

    @Test
    public void firstDifferentBit_isOk() {
        //Return BIT_LENGTH if addresses are equals
        Assert.assertEquals(KADAddress.BIT_LENGTH, KADAddress.firstDifferentBit(KAD_ADDRESS_1, KAD_ADDRESS_1));

        Assert.assertEquals(FIRST_DIFF_BIT_INDEX_EXPECTED, KADAddress.firstDifferentBit(KAD_ADDRESS_2, KAD_ADDRESS_3));
    }

    @Test
    public void equals_isOk() {
        Assert.assertTrue(KAD_ADDRESS_1.equals(KAD_ADDRESS_1));
        Assert.assertFalse(KAD_ADDRESS_1.equals(KAD_ADDRESS_2));
    }

    @Test
    public void closerToTarget_isOk() {
        Assert.assertEquals(KAD_ADDRESS_5, KADAddress.closerToTarget(KAD_ADDRESS_4, KAD_ADDRESS_5, KAD_ADDRESS_1));

        //Returns the first KADAddress param passed
        Assert.assertEquals(KAD_ADDRESS_4, KADAddress.closerToTarget(KAD_ADDRESS_4, KAD_ADDRESS_4_2, KAD_ADDRESS_1));
    }

}
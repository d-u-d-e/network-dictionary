
package com.eis.networklibrary.kademlia;


import org.junit.Assert;
import org.junit.Test;

/**
 * @author Marco Mariotto
 * @author Alberto Ursino
 */
public class KADAddressTest {

    private KADAddress ADDRESS_1 = new KADAddress(new byte[]{106, 97, 118, 97, 32, 105, 115, 32, 111, 107});
    private KADAddress ADDRESS_2 = new KADAddress(new byte[]{106, 97, 118, 97, 32, 105, 115, 32, 111, 107});
    private KADAddress ADDRESS_3 = new KADAddress(new byte[]{12, 3, 118, 97, 32, 105, 115, 32, 111, 107});
    private KADAddress ADDRESS_4 = new KADAddress(new byte[]{12, 8, 118, 97, 32, 105, 115, 32, 111, 107});
    private KADAddress ADDRESS_5 = new KADAddress(new byte[]{106, 97, 118, 97, 32, 85, 115, 32, 111, 107});
    private KADAddress ADDRESS_6 = new KADAddress(new byte[]{106, 97, 118, 97, 32, 99, 115, 32, 111, 107});
    private final String ADDRESS_1_TO_STRING = "6A617661206973206F6B";

    @Test
    public void toString_test() {
        Assert.assertEquals(ADDRESS_1_TO_STRING, ADDRESS_1.toString());
    }

    @Test
    public void fromHexString_test() {
        Assert.assertEquals(ADDRESS_1, KADAddress.fromHexString(ADDRESS_1_TO_STRING));
    }

    @Test
    public void firstDifferentBit_test() {
        Assert.assertEquals(KADAddress.BIT_LENGTH, KADAddress.firstDifferentBit(ADDRESS_1, ADDRESS_2));
        Assert.assertEquals(12, KADAddress.firstDifferentBit(ADDRESS_3, ADDRESS_4));
    }

    @Test
    public void equals_test(){
        Assert.assertEquals(ADDRESS_1, ADDRESS_2);
    }

    @Test
    public void closerToTarget_test(){
        Assert.assertEquals(ADDRESS_6, KADAddress.closerToTarget(ADDRESS_5, ADDRESS_6, ADDRESS_1));
    }

    @Test
    public void generalTest() {

    }

}
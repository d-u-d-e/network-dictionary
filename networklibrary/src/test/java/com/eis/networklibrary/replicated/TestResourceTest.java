package com.eis.networklibrary.replicated;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Marco Mariotto
 */
public class TestResourceTest {

    @Test
    public void equals_isTrue() {
        TestResource res1 = new TestResource("CIAO");
        TestResource res2 = new TestResource("CIAO");
        Assert.assertTrue(res1.equals(res2));
    }

}
package com.eis.networklibrary.distributed;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SMSDistributedNetworkDictionaryTest {

    private static String DEFAULT_TELEPHONE_NUMBER = "+393343343332";
    private static KADPeer DEFAULT_USER_ADDRESS = new KADPeer(DEFAULT_TELEPHONE_NUMBER);
    private static KADPeer OTHER_USER_ADDRESS = new KADPeer(DEFAULT_TELEPHONE_NUMBER + "3");
    private static String DEFAULT_RESOURCE_KEY_1 = "Resource key number one";
    private static String DEFAULT_RESOURCE_VALUE_1 = "This is the value";

    private SMSDistributedNetworkDictionary<String,String> defaultDictionary;

    @Before
    public void init() {
        defaultDictionary = new SMSDistributedNetworkDictionary<>(DEFAULT_USER_ADDRESS);
        defaultDictionary.addUser(OTHER_USER_ADDRESS);
        defaultDictionary.setResource(DEFAULT_RESOURCE_KEY_1, DEFAULT_RESOURCE_VALUE_1);
        System.out.println("Closeness: " + DEFAULT_USER_ADDRESS.firstDifferentBit(OTHER_USER_ADDRESS));
    }

    @Test
    public void getAllUsers() {
        Assert.assertEquals(OTHER_USER_ADDRESS, defaultDictionary.getAllUsers().get(0));
    }

    @Test
    public void removeUser_presentUser_isOk() {
        defaultDictionary.removeUser(OTHER_USER_ADDRESS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeUser_notPresentUser_throwsException() {
        defaultDictionary.removeUser(DEFAULT_USER_ADDRESS);
    }

}
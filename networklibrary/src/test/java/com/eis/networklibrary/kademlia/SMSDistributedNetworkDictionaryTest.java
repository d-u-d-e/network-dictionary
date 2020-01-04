package com.eis.networklibrary.kademlia;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @author Alessandra Tonin, Luca Crema
 */
public class SMSDistributedNetworkDictionaryTest {

    private static String DEFAULT_TELEPHONE_NUMBER_1 = "+393343343332";
    private static String DEFAULT_TELEPHONE_NUMBER_2 = "+393343343335";
    private static String DEFAULT_TELEPHONE_NUMBER_3 = "+393343347732";
    private static String DEFAULT_TELEPHONE_NUMBER_4 = "+393643343332";
    private static String DEFAULT_TELEPHONE_NUMBER_5 = "+393383343332";
    private static String DEFAULT_TELEPHONE_NUMBER_6 = "+393343343032";
    private static SMSKADPeer DEFAULT_USER_1_ADDRESS = new SMSKADPeer(DEFAULT_TELEPHONE_NUMBER_1);
    private static SMSKADPeer DEFAULT_USER_2_ADDRESS = new SMSKADPeer(DEFAULT_TELEPHONE_NUMBER_2);
    private static SMSKADPeer DEFAULT_USER_3_ADDRESS = new SMSKADPeer(DEFAULT_TELEPHONE_NUMBER_3);
    private static SMSKADPeer DEFAULT_USER_4_ADDRESS = new SMSKADPeer(DEFAULT_TELEPHONE_NUMBER_4);
    private static SMSKADPeer DEFAULT_USER_5_ADDRESS = new SMSKADPeer(DEFAULT_TELEPHONE_NUMBER_5);
    private static SMSKADPeer DEFAULT_USER_6_ADDRESS = new SMSKADPeer(DEFAULT_TELEPHONE_NUMBER_6); //to add a peer when bucket is full
    private static SMSKADPeer OTHER_USER_ADDRESS = new SMSKADPeer(DEFAULT_TELEPHONE_NUMBER_1 + "3");
    private static final SMSKADPeer[] ALL_USERS = {OTHER_USER_ADDRESS, DEFAULT_USER_2_ADDRESS, DEFAULT_USER_3_ADDRESS, DEFAULT_USER_4_ADDRESS};
    private static KADAddress DEFAULT_RESOURCE_KEY_1 = new KADAddress("Resource key number one");
    private static String DEFAULT_RESOURCE_VALUE_1 = "This is the value number one";
    private static KADAddress DEFAULT_RESOURCE_KEY_2 = new KADAddress("Resource key number two");
    private static String DEFAULT_RESOURCE_VALUE_2 = "This is the value number two";
    private static KADAddress DEFAULT_RESOURCE_KEY_3 = new KADAddress("Resource key number three");
    private static String DEFAULT_RESOURCE_VALUE_3 = "This is the value number three";


    private SMSDistributedNetworkDictionary<String> defaultDictionary;

    /*
     * Methods to compare Arrays or ArrayLists
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

    private <K, V> boolean compareHashMaps(Map<K, V> map1, Map<K, V> map2) {
        if (map1.values().size() != map2.values().size())
            return false;
        for (K key : map1.keySet()) {
            if (!map2.containsKey(key))
                return false;
        }
        return true;
    }

    @Before
    public void init() {
        defaultDictionary = new SMSDistributedNetworkDictionary<>(DEFAULT_USER_1_ADDRESS);
        defaultDictionary.addUser(OTHER_USER_ADDRESS);
        defaultDictionary.setResource(DEFAULT_RESOURCE_KEY_1, DEFAULT_RESOURCE_VALUE_1);
        System.out.println("Closeness: " + KADAddress.firstDifferentBit(DEFAULT_USER_1_ADDRESS.getNetworkAddress(), OTHER_USER_ADDRESS.getNetworkAddress()));
    }

    @Test
    public void getAllUsersTest() {
        Assert.assertEquals(OTHER_USER_ADDRESS, defaultDictionary.getAllUsers().get(0));
    }

    @Test
    public void addAllUsersTest() {
        SMSKADPeer[] users = {DEFAULT_USER_2_ADDRESS, DEFAULT_USER_3_ADDRESS, DEFAULT_USER_4_ADDRESS};
        List<SMSKADPeer> list = new ArrayList<>(Arrays.asList(users));
        defaultDictionary.addAllUsers(list);
        Assert.assertTrue(compareArray(ALL_USERS, defaultDictionary.getAllUsers()));
    }

    @Test
    public void removeUser_presentUser_isOk() {
        defaultDictionary.removeUser(OTHER_USER_ADDRESS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeUser_notPresentUser_throwsException() {
        defaultDictionary.removeUser(DEFAULT_USER_5_ADDRESS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeUser_mySelf_throwsException() {
        defaultDictionary.removeUser(DEFAULT_USER_1_ADDRESS);
    }

    @Test
    public void removeAllUsersTest() {
        defaultDictionary.addUser(DEFAULT_USER_2_ADDRESS);
        defaultDictionary.addUser(DEFAULT_USER_3_ADDRESS);
        defaultDictionary.addUser(DEFAULT_USER_4_ADDRESS);
        SMSKADPeer[] users = {DEFAULT_USER_2_ADDRESS, DEFAULT_USER_3_ADDRESS, DEFAULT_USER_4_ADDRESS};
        List<SMSKADPeer> list = new ArrayList<>(Arrays.asList(users));
        defaultDictionary.removeAllUsers(list);
        Assert.assertEquals(OTHER_USER_ADDRESS, defaultDictionary.getAllUsers().get(0));
    }

    @Test
    public void generateRandomAddressInSubtreeTest() {
        System.out.println(DEFAULT_USER_1_ADDRESS.getNetworkAddress());
        for (int i = 0; i < 5; i++)
            System.out.println(defaultDictionary.generateRandomAddressInSubtree(i));
    }

    @Test
    public void getUsersInBucketTest() {
        int bucketIndex = defaultDictionary.getBucketContaining(OTHER_USER_ADDRESS.getNetworkAddress());
        SMSKADPeer[] userInBucket = {OTHER_USER_ADDRESS};
        Assert.assertTrue(compareArray(userInBucket, defaultDictionary.getUsersInBucket(bucketIndex)));
    }

    @Test
    public void getUsersInEmptyBucketTest() {
        int bucketIndex = defaultDictionary.getBucketContaining(OTHER_USER_ADDRESS.getNetworkAddress());
        System.out.println(bucketIndex); //it is in bucket 77
        ArrayList<SMSKADPeer> emptyBucket = new ArrayList<>();
        Assert.assertTrue(compareArray(emptyBucket, defaultDictionary.getUsersInBucket(15)));
    }

    @Test //set a new resource
    public void setNewResourceTest() {
        String nullResource = null;
        Assert.assertEquals(nullResource, defaultDictionary.setResource(DEFAULT_RESOURCE_KEY_2, DEFAULT_RESOURCE_VALUE_2));
    }

    @Test //set a new value for an existing key
    public void setOldResourceTest() {
        String oldResource = DEFAULT_RESOURCE_VALUE_1;
        Assert.assertEquals(oldResource, defaultDictionary.setResource(DEFAULT_RESOURCE_KEY_1, DEFAULT_RESOURCE_VALUE_2));
    }

    @Test
    public void setAllNewResourcesTest() {
        Map<KADAddress, String> resources = new HashMap<>();
        resources.put(DEFAULT_RESOURCE_KEY_3, DEFAULT_RESOURCE_VALUE_3);
        resources.put(DEFAULT_RESOURCE_KEY_1, DEFAULT_RESOURCE_VALUE_2);
        Map<KADAddress, String> expectedResources = new HashMap<>();
        expectedResources.put(DEFAULT_RESOURCE_KEY_3, null);
        expectedResources.put(DEFAULT_RESOURCE_KEY_1, DEFAULT_RESOURCE_VALUE_1);
        Assert.assertTrue(compareHashMaps(expectedResources, defaultDictionary.setAllResources(resources)));
    }

    @Test
    public void removeResourceTest() {
        Assert.assertEquals(DEFAULT_RESOURCE_VALUE_1, defaultDictionary.removeResource(DEFAULT_RESOURCE_KEY_1));
    }

    @Test
    public void removeAllResourcesTest() {
        defaultDictionary.setResource(DEFAULT_RESOURCE_KEY_3, DEFAULT_RESOURCE_VALUE_3);
        KADAddress[] resources = {DEFAULT_RESOURCE_KEY_1, DEFAULT_RESOURCE_KEY_3};
        List<KADAddress> list = new ArrayList<>(Arrays.asList(resources));
        ArrayList<String> removedValues = new ArrayList<>();
        removedValues.add(DEFAULT_RESOURCE_VALUE_1);
        removedValues.add(DEFAULT_RESOURCE_VALUE_3);
        Assert.assertEquals(removedValues, defaultDictionary.removeAllResources(list));
    }

    @Test
    public void getValueTest() {
        Assert.assertEquals(DEFAULT_RESOURCE_VALUE_1, defaultDictionary.getValue(DEFAULT_RESOURCE_KEY_1));
    }

    @Test
    public void getAllValuesTest() {
        defaultDictionary.setResource(DEFAULT_RESOURCE_KEY_2, DEFAULT_RESOURCE_VALUE_2);
        ArrayList<String> values = new ArrayList<>();
        values.add(DEFAULT_RESOURCE_VALUE_1);
        values.add(DEFAULT_RESOURCE_VALUE_2);
        KADAddress[] keys = {DEFAULT_RESOURCE_KEY_1, DEFAULT_RESOURCE_KEY_2};
        List<KADAddress> list = new ArrayList<>(Arrays.asList(keys));
        Assert.assertTrue(compareArray(values, defaultDictionary.getAllValues(list)));
    }

    @Test
    public void getKeysTest() {
        Assert.assertEquals(DEFAULT_RESOURCE_KEY_1, defaultDictionary.getKeys().get(0));
    }

    @Test
    public void getValuesTest() {
        Assert.assertEquals(DEFAULT_RESOURCE_VALUE_1, defaultDictionary.getValues().get(0));
    }

}


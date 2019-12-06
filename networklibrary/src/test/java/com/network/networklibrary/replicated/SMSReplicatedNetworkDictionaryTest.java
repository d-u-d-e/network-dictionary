package com.network.networklibrary.replicated;

import com.eis.smslibrary.SMSPeer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * @author Marco Mariotto
 */
public class SMSReplicatedNetworkDictionaryTest {

    private static final String DEFAULT_PEER_ADDRESS = "+393434444543";
    private static final String DEFAULT_RESOURCE_ID = "DefaultResourceID";

    private static final SMSPeer DEFAULT_USER_1 = new SMSPeer(DEFAULT_PEER_ADDRESS);
    private static final SMSPeer DEFAULT_USER_2 = new SMSPeer(DEFAULT_PEER_ADDRESS + "1");
    private static final SMSPeer DEFAULT_USER_3 = new SMSPeer(DEFAULT_PEER_ADDRESS + "2");
    private static final SMSPeer DEFAULT_USER_4 = new SMSPeer("+393434444544");
    private static final SMSPeer COPY_USER_1 = new SMSPeer(DEFAULT_PEER_ADDRESS);
    private static final TestResource DEFAULT_RESOURCE_1 = new TestResource(DEFAULT_RESOURCE_ID);
    private static final TestResource DEFAULT_RESOURCE_2 = new TestResource(DEFAULT_RESOURCE_ID + "1");
    private static final TestResource DEFAULT_RESOURCE_3 = new TestResource(DEFAULT_RESOURCE_ID + "2");
    private static final TestResource DEFAULT_RESOURCE_4 = new TestResource(DEFAULT_RESOURCE_ID + "3");
    private static final TestResource[] DEFAULT_RESOURCE_ARRAY = new TestResource[]{DEFAULT_RESOURCE_1, DEFAULT_RESOURCE_2};
    private static final SMSPeer[] DEFAULT_USER_ARRAY = new SMSPeer[]{DEFAULT_USER_1, DEFAULT_USER_2};
    private static final SMSPeer[] DEFAULT_EXCLUDED_USER_ARRAY = new SMSPeer[]{DEFAULT_USER_3, DEFAULT_USER_4};
    private static final SMSPeer[] ALL_USER_ARRAY = new SMSPeer[]{DEFAULT_USER_1, DEFAULT_USER_2, DEFAULT_USER_3, DEFAULT_USER_4};

    private SMSReplicatedNetworkDictionary<SMSPeer, TestResource> defaultDictionary;

    /**
     * @throws Exception ??? Auto-generated from Android Studio
     */
    @Before
    public void setUp() throws Exception {
        defaultDictionary = new SMSReplicatedNetworkDictionary<>();
        defaultDictionary.addUser(DEFAULT_USER_2);
        defaultDictionary.addUser(DEFAULT_USER_1);
        defaultDictionary.setResource(DEFAULT_USER_1, DEFAULT_RESOURCE_1);
        defaultDictionary.setResource(DEFAULT_USER_2, DEFAULT_RESOURCE_2);
    }

    /**
     * Compares the elements of two arrays as a set (doesn't care about position)
     *
     * @param array1
     * @param array2
     * @return
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

    @Test
    public void getAllUsers_user_isEquals() {
        Assert.assertTrue(compareArray(DEFAULT_USER_ARRAY, defaultDictionary.getAllUsers()));
    }

    @Test
    public void getAllResources_resources_areEquals() {
        ArrayList<TestResource> allResources = defaultDictionary.getValues();
        Assert.assertTrue(compareArray(DEFAULT_RESOURCE_ARRAY, allResources));
    }

    @Test
    public void removeUser_users_areEquals() {
        defaultDictionary.removeUser(DEFAULT_USER_1);
        Assert.assertTrue(compareArray(new SMSPeer[]{DEFAULT_USER_2}, defaultDictionary.getAllUsers()));
    }

    @Test
    public void removeUser_users_areEquals2() {
        defaultDictionary.removeUser(DEFAULT_USER_2);
        Assert.assertTrue(compareArray(new SMSPeer[]{DEFAULT_USER_1}, defaultDictionary.getAllUsers()));
    }

    @Test
    public void removeUser_nonExistentUser_users_areEquals() {
        defaultDictionary.removeUser(DEFAULT_USER_3);
        Assert.assertTrue(compareArray(DEFAULT_USER_ARRAY, defaultDictionary.getAllUsers()));
    }

    @Test
    public void removeUser_users_isEmpty() {
        defaultDictionary.removeUser(DEFAULT_USER_1);
        defaultDictionary.removeUser(DEFAULT_USER_2);
        Assert.assertTrue(compareArray(new SMSPeer[0], defaultDictionary.getAllUsers()));
    }

    @Test
    public void getValue_value_isEquals() {
        Assert.assertEquals(DEFAULT_RESOURCE_1, defaultDictionary.getValue(DEFAULT_USER_1));
    }

    @Test
    public void getValue_value_isEquals2() {
        Assert.assertEquals(DEFAULT_RESOURCE_2, defaultDictionary.getValue(DEFAULT_USER_2));
    }

    @Test
    public void getValue_nonExistentKey_resource_isEmpty() {
        Assert.assertNull(defaultDictionary.getValue(DEFAULT_USER_3));
    }

    @Test
    public void addUser_duplicate_isNotInserted() {
        defaultDictionary.addUser(DEFAULT_USER_1);
        Assert.assertTrue(compareArray(DEFAULT_USER_ARRAY, defaultDictionary.getAllUsers()));
    }

    @Test
    public void addUser_almostDuplicate_isNotInserted() {
        defaultDictionary.addUser(COPY_USER_1);
        Assert.assertTrue(compareArray(DEFAULT_USER_ARRAY, defaultDictionary.getAllUsers()));
    }

    @Test
    public void getAllUsers_users_areOrdered() {
        defaultDictionary.addUser(DEFAULT_USER_4);
        SMSPeer[] testOrderedArray = new SMSPeer[]{DEFAULT_USER_1, DEFAULT_USER_2, DEFAULT_USER_4};
        Assert.assertArrayEquals(testOrderedArray, defaultDictionary.getAllUsers().toArray());
    }

    @Test
    public void removeResource_keys_areEquals() {
        Assert.assertNotNull(defaultDictionary.removeResource(DEFAULT_USER_2));
        Assert.assertTrue(compareArray(new SMSPeer[]{DEFAULT_USER_1}, defaultDictionary.getKeys()));
    }

    @Test
    public void removeResource_values_areEquals() {
        Assert.assertNotNull(defaultDictionary.removeResource(DEFAULT_USER_2));
        Assert.assertTrue(compareArray(new TestResource[]{DEFAULT_RESOURCE_1}, defaultDictionary.getValues()));
    }

    @Test
    public void getAllKeys_keys_areEquals() {
        Assert.assertTrue(compareArray(DEFAULT_USER_ARRAY, defaultDictionary.getKeys()));
    }

    @Test
    public void addAllUsers_users_areEquals() {
        defaultDictionary.addAllUsers(Arrays.asList(DEFAULT_EXCLUDED_USER_ARRAY));
        Assert.assertTrue(compareArray(ALL_USER_ARRAY, defaultDictionary.getAllUsers()));
    }

    @Test
    public void addAllUsers_emptyUsers_users_areEquals() {
        defaultDictionary.addAllUsers(Arrays.asList(new SMSPeer[0]));
        Assert.assertTrue(compareArray(DEFAULT_USER_ARRAY, defaultDictionary.getAllUsers()));
    }

    @Test
    public void addAllUsers_sameUsers_users_areEquals() {
        defaultDictionary.addAllUsers(Arrays.asList(DEFAULT_USER_1));
        Assert.assertTrue(compareArray(DEFAULT_USER_ARRAY, defaultDictionary.getAllUsers()));
    }

    @Test
    public void removeAllUsers_emptyList_users_areEquals() {
        defaultDictionary.removeAllUsers(Arrays.asList(new SMSPeer[0]));
        Assert.assertTrue(compareArray(DEFAULT_USER_ARRAY, defaultDictionary.getAllUsers()));
    }

    @Test
    public void removeAllUsers_users_areEquals() {
        defaultDictionary.removeAllUsers(Arrays.asList(DEFAULT_USER_1));
        Assert.assertTrue(compareArray(new SMSPeer[]{DEFAULT_USER_2}, defaultDictionary.getAllUsers()));
    }

    @Test
    public void setAllResources_resources_areEquals() {
        HashMap<SMSPeer, TestResource> testResourceHashMap = new HashMap<>();
        testResourceHashMap.put(DEFAULT_USER_3, DEFAULT_RESOURCE_3);
        testResourceHashMap.put(DEFAULT_USER_4, DEFAULT_RESOURCE_4);

        HashMap<SMSPeer, TestResource> resultHashMap = new HashMap<>();
        resultHashMap.put(DEFAULT_USER_3, null);
        resultHashMap.put(DEFAULT_USER_4, null);

        Assert.assertTrue(compareHashMaps(resultHashMap, defaultDictionary.setAllResources(testResourceHashMap)));
        Assert.assertTrue(compareArray(ALL_USER_ARRAY, defaultDictionary.getKeys()));
    }

    @Test
    public void removeAllResources_resources_areEquals() {
        defaultDictionary.removeAllResources(Arrays.asList(DEFAULT_USER_1));
        Assert.assertTrue(compareArray(new SMSPeer[]{DEFAULT_USER_2}, defaultDictionary.getKeys()));
    }

    @Test
    public void removeAllResources_allResources_resources_areEquals() {
        defaultDictionary.removeAllResources(Arrays.asList(DEFAULT_USER_ARRAY));
        Assert.assertTrue(compareArray(new SMSPeer[0], defaultDictionary.getKeys()));
    }

    @Test
    public void getAllValues_allUsers_values_areEquals() {
        Assert.assertTrue(compareArray(DEFAULT_RESOURCE_ARRAY, defaultDictionary.getAllValues(Arrays.asList(DEFAULT_USER_ARRAY))));
    }

    @Test
    public void getAllValues_oneUser_values_areEquals() {
        Assert.assertTrue(compareArray(new TestResource[]{DEFAULT_RESOURCE_1}, defaultDictionary.getAllValues(Arrays.asList(DEFAULT_USER_1))));
    }

    @Test
    public void test_compareArray_isCorrect() {
        TestResource[] arr1 = new TestResource[]{DEFAULT_RESOURCE_1, DEFAULT_RESOURCE_2};
        TestResource[] arr2 = new TestResource[]{DEFAULT_RESOURCE_2, DEFAULT_RESOURCE_1};
        Assert.assertTrue(compareArray(arr1, arr2));
    }

}
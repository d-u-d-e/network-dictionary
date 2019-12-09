package com.eis.networklibrary.replicated;


import com.eis.smslibrary.SMSPeer;
import com.eis.communication.NetworkDictionary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Network dictionary for peers and resources on the SMS network
 *
 * @param <RK> resource key
 * @param <RV> resource value
 * @author Luca Crema, Marco Mariotto
 */
public class SMSReplicatedNetworkDictionary<RK, RV> implements NetworkDictionary<SMSPeer, RK, RV> {

    /**
     * Sorted user list
     */
    private ArrayList<SMSPeer> userList;

    private HashMap<RK, RV> resourcesDict;

    /**
     * Constructor for the dictionary
     */
    protected SMSReplicatedNetworkDictionary() {
        userList = new ArrayList<>();
        resourcesDict = new HashMap<>();
    }

    /**
     * Registers a user to the network
     *
     * @param user new network user
     */
    @Override
    public void addUser(SMSPeer user) {
        if (userList.contains(user))
            return;
        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).compareTo(user) > 0) {
                userList.add(i, user);
                return;
            }
        }
        //If the array is empty or the position to insert the user is the last one
        userList.add(user);
    }

    /**
     * Adds a collection of users to the network
     *
     * @param users new network users
     */
    @Override
    public void addAllUsers(Collection<SMSPeer> users) {
        for (SMSPeer user : users) {
            addUser(user);
        }
    }

    /**
     * @return a list of all current users in the network
     */
    @Override
    public ArrayList<SMSPeer> getAllUsers() {
        return userList;
    }

    /**
     * Removes a SMS user from the network
     *
     * @param user to be removed
     */
    @Override
    public void removeUser(SMSPeer user) {
        userList.remove(user);
    }

    /**
     * Removes a collection of SMS users from the network
     *
     * @param users registered users
     */
    @Override
    public void removeAllUsers(Collection<SMSPeer> users) {
        for (SMSPeer user : users) {
            removeUser(user);
        }
    }

    /**
     * Sets the value of the key to the resource value
     *
     * @param key   identification for the resource
     * @param value of the resource
     * @return the previous resource value if there was one, null otherwise
     */
    @Override
    public RV setResource(RK key, RV value) {
        return resourcesDict.put(key, value);
    }

    /**
     * Sets the value for the keys for the entries of a map
     *
     * @param resources a map of key and values
     * @return {@link Map} of edited values, each key contains the previous resource if there was one, null otherwise
     */
    @Override
    public Map<RK, RV> setAllResources(Map<RK, RV> resources) {
        Map<RK, RV> editedMap = new HashMap<>();
        for (Map.Entry entry : resources.entrySet()) {
            editedMap.put((RK) entry.getKey(), setResource((RK) entry.getKey(), (RV) entry.getValue()));
        }
        return editedMap;
    }

    /**
     * Removes the resource from the network
     *
     * @param resourceKey identification for the resource
     * @return the removed resource value
     */
    @Override
    public RV removeResource(RK resourceKey) {
        return resourcesDict.remove(resourceKey);
    }

    /**
     * Removes a resource collection from the network
     *
     * @param resourcesKeys identification keys for the resources
     * @return {@link ArrayList} of removed resources
     */
    @Override
    public ArrayList<RV> removeAllResources(Collection<RK> resourcesKeys) {
        ArrayList<RV> removedResources = new ArrayList<>();
        for (RK key : resourcesKeys) {
            removedResources.add(removeResource(key));
        }
        return removedResources;
    }

    /**
     * Gets the value given a key
     *
     * @param resourceKey identification for the resource
     * @return the requested value, null if not present
     */
    @Override
    public RV getValue(RK resourceKey) {
        return resourcesDict.get(resourceKey);
    }

    /**
     * Gets the values given a collection of keys
     *
     * @param resourceKeys identifications for the resources
     * @return requested resources if present
     */
    @Override
    public ArrayList<RV> getAllValues(Collection<RK> resourceKeys) {
        ArrayList<RV> values = new ArrayList<>();
        for (RK key : resourceKeys) {
            values.add(getValue(key));
        }
        return values;
    }

    /**
     * Retrieves all keys
     *
     * @return {@link ArrayList} of keys in the dictionary
     */
    @Override
    public ArrayList<RK> getKeys() {
        return new ArrayList<>(resourcesDict.keySet());
    }

    /**
     * Retrieves all values
     *
     * @return {@link ArrayList} of values in the dictionary
     */
    @Override
    public ArrayList<RV> getValues() {
        return new ArrayList<>(resourcesDict.values());
    }
}

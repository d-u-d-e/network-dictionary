package com.network.communication;

import com.eis.communication.Peer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * @param <U>  Peer for users in the network
 * @param <RK> Key class identifier for the resource
 * @param <RV> Value class for the resource
 * @author Group 4
 */
public interface NetworkDictionary<U extends Peer, RK, RV> {

    /**
     * Registers a user to the network
     *
     * @param user new network user
     */
    void addUser(U user);

    /**
     * Registers all the users from a collection to the network
     *
     * @param users new network users
     */
    void addAllUsers(Collection<U> users);

    /**
     * Get the whole list of users from the network
     *
     * @return a list of users in the network
     */
    ArrayList<U> getAllUsers();

    /**
     * Removes the user from the network
     *
     * @param user registered user
     */
    void removeUser(U user);

    /**
     * Removes the users from the network
     *
     * @param users registered users
     */
    void removeAllUsers(Collection<U> users);

    /**
     * Sets the value of the key to the resource value
     *
     * @param key   a resource key present in the dictionary
     * @param value updated value of the resource
     * @return the previous resource value if there was one, null otherwise
     */
    RV setResource(RK key, RV value);

    /**
     * Sets the value of the keys to the resources values
     *
     * @param resources a map of key and values
     * @return the previous resource values if they were overwritten, empty map otherwise
     */
    Map<RK, RV> setAllResources(Map<RK, RV> resources);

    /**
     * Removes a resource availability from the network
     *
     * @param resourceKey identification for the resource
     * @return the removed resource value
     */
    RV removeResource(RK resourceKey);

    /**
     * Removes a resource availability from the network
     *
     * @param resourcesKeys identification keys for the resources
     * @return the removed resources values
     */
    ArrayList<RV> removeAllResources(Collection<RK> resourcesKeys);

    /**
     * Get the value given a key
     *
     * @param resourceKey identification for the resource
     * @return requested resource if present
     */
    RV getValue(RK resourceKey);

    /**
     * Get the values given a collection of keys
     *
     * @param resourceKeys identifications for the resources
     * @return requested resources if present
     */
    ArrayList<RV> getAllValues(Collection<RK> resourceKeys);

    /**
     * Get the whole list of users from the network
     *
     * @return a list of users in the network
     */
    ArrayList<RK> getKeys();

    /**
     * Get all the resources available in the network
     *
     * @return a list of resources in the network
     */
    ArrayList<RV> getValues();
}

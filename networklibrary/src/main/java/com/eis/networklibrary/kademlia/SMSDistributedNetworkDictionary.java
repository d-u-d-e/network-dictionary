package com.eis.networklibrary.kademlia;

import com.eis.communication.network.NetworkDictionary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Distributed network dictionary for peers and resources on the SMS network (Kademlia method)
 *
 * @param <RV> resource value
 * @author Luca Crema, Marco Mariotto
 */
public class SMSDistributedNetworkDictionary<RV> implements NetworkDictionary<SMSKADPeer, KADAddress, RV> {

    /**
     * Maximum users per bucket
     */
    static final int BUCKET_SIZE = 5; // this is KAD K constant TODO: Use this
    static final int NO_BUCKETS = KADAddress.BYTE_ADDRESS_LENGTH * Byte.SIZE; //we have a bucket for each bit
    SMSKADPeer mySelf; //address of current node holding this dictionary
    private ArrayList<SMSKADPeer>[] buckets;
    private HashMap<KADAddress, RV> resourcesDict;

    /**
     * Constructor for the dictionary
     *
     * @param mySelf my current address
     */
    public SMSDistributedNetworkDictionary(SMSKADPeer mySelf) {
        this.mySelf = mySelf;
        buckets = new ArrayList[NO_BUCKETS];
    }

    /**
     * Adds a user at the end of the corresponding bucket to contact.
     *
     * @param newUser new network user. Must not be the current user.
     */
    @Override
    public void addUser(SMSKADPeer newUser) {

        //bucketIndex is the closer bucket of mySelf containing newUser
        int bucketIndex = getBucketContaining(newUser.getNetworkAddress());

        //If it's actually the current user we don't add himself
        if (bucketIndex == NO_BUCKETS) return;

        if (buckets[bucketIndex] == null)
            buckets[bucketIndex] = new ArrayList<>();
        else if (buckets[bucketIndex].contains(newUser))
            return;

        //TODO each bucket should contain at most MAX_USER_BUCKET_LENGTH users: use an array? and add politics of queuing
        buckets[bucketIndex].add(newUser);
    }


    int getBucketContaining(KADAddress address){

        //The bucket of node X which has index i contains nodes whose xor distance to X is between 2^i inclusive and 2^(i+1) exclusive.
        //For example, if i = 0, then bucket 0 contains the only node whose distance to X is 1 (thus it has the last bit flipped and it is the closer to X
        //even from a geometric point of view in the tree). The closer bucket of mySelf containing address is therefore:

        return NO_BUCKETS - 1 - mySelf.getNetworkAddress().firstDifferentBit(address);
        //returns NO_BUCKETS if address is equal to mySelf
    }

    /**
     * Adds the collection of users to the end of the user list
     *
     * @param users new network users
     */
    @Override
    public void addAllUsers(Collection<SMSKADPeer> users) {
        for (SMSKADPeer user : users) {
            addUser(user);
        }
    }

    /**
     * Returns an array of all users indexed.
     * Use with caution, they are not divided by bucket.
     *
     * @return a list of all users.
     */
    @Override
    public ArrayList<SMSKADPeer> getAllUsers() {
        ArrayList<SMSKADPeer> returnList = new ArrayList<>();

        for (int i = 0; i < NO_BUCKETS; i++) {
            if (buckets[i] != null)
                returnList.addAll(buckets[i]);
        }
        return returnList;
    }

    /**
     * @param bucketIndex identifies each bucket, from 0 to N-1, where N = NO_BUCKETS.
     *                    Note that if bucketIndex = i, for i between 0 and N-1, then buckets[i] contains all known nodes of distance (XOR metric)
     *                    between 2^(i) inclusive and 2^(i+1) exclusive. For instance if i = 0,
     *                    then we get all nodes whose distant d from myself is >= 2^0 = 1 and < 2, that is d = 1.
     *                    then we get all nodes. The only node satisfying this is the node having all first N-1 significant bits equal to those of mySelf,
     *                    except for the last bit which is flipped.
     *                    If otherwise i = N-1, we get all nodes whose distance d from mySelf is >= 2^(N-1) and < 2^(N), meaning that the first significant bit is flipped.
     *
     * @return an ArrayList of users in that particular bucket, empty ArrayList if there is none
     */
    public ArrayList<SMSKADPeer> getUsersInBucket(int bucketIndex) {
        if (buckets[bucketIndex] != null)
            return new ArrayList<>(buckets[bucketIndex]);
        return new ArrayList<>();
    }

    SMSKADPeer getPeerFromAddress(KADAddress address) {
        for (SMSKADPeer peer : buckets[getBucketContaining(address)])
            if (peer.getNetworkAddress().equals(address))
                return peer;
        return null;
    }

    public ArrayList<SMSKADPeer> getAllUsersSortedByClosestTo(KADAddress address){
       ArrayList<SMSKADPeer> users = getAllUsers();
       Collections.sort(users, new SMSKADPeer.KADComparator(address));
       return users;
    }

    /**
     * Removes a user from the list of peers
     *
     * @param user registered user
     */
    @Override
    public void removeUser(SMSKADPeer user) {
        int bucketIndex =getBucketContaining(user.getNetworkAddress());
        if (bucketIndex == NO_BUCKETS)
            throw new IllegalArgumentException("Cannot remove itself");
        if (buckets[bucketIndex] == null)
            throw new IllegalArgumentException("User is not actually present in the list");
        if (!buckets[bucketIndex].remove(user))
            throw new IllegalArgumentException("User is not actually present in the user list");
    }

    /**
     * Removes a collection of users from the list of peers
     *
     * @param users registered users
     */
    @Override
    public void removeAllUsers(Collection<SMSKADPeer> users) {
        for (SMSKADPeer user : users) {
            removeUser(user);
        }
    }

    /**
     * @param key   a resource key present in the dictionary
     * @param value updated value of the resource
     * @return the old value of the resource if there was one, null otherwise
     */
    @Override
    public RV setResource(KADAddress key, RV value) {
        return resourcesDict.put(key, value);
    }

    /**
     * @param resources a map of key and values
     * @return the old values of the resources if there were one, contains null otherwise
     */
    @Override
    public Map<KADAddress, RV> setAllResources(Map<KADAddress, RV> resources) {
        Map<KADAddress, RV> editedMap = new HashMap<>();
        for (Map.Entry entry : resources.entrySet()) {
            editedMap.put((KADAddress) entry.getKey(), setResource((KADAddress) entry.getKey(), (RV) entry.getValue()));
        }
        return editedMap;
    }

    /**
     * @param resourceKey identification for the resource
     * @return the value of the removed resource
     */
    @Override
    public RV removeResource(KADAddress resourceKey) {
        return resourcesDict.remove(resourceKey);
    }

    /**
     * @param resourcesKeys identification keys for the resources
     * @return the values of the removed resources
     */
    @Override
    public ArrayList<RV> removeAllResources(Collection<KADAddress> resourcesKeys) {
        ArrayList<RV> removedResources = new ArrayList<>();
        for (KADAddress key : resourcesKeys) {
            removedResources.add(removeResource(key));
        }
        return removedResources;
    }

    /**
     * @param resourceKey identification for the resource
     * @return the value of the given resource key, null otherwise
     */
    @Override
    public RV getValue(KADAddress resourceKey) {
        return resourcesDict.get(resourceKey);
    }

    /**
     * @param resourceKeys identifications for the resources
     * @return a {@link ArrayList} of all values associated to the given keys
     */
    @Override
    public ArrayList<RV> getAllValues(Collection<KADAddress> resourceKeys) {
        ArrayList<RV> values = new ArrayList<>();
        for (KADAddress key : resourceKeys) {
            values.add(getValue(key));
        }
        return values;
    }

    /**
     * @return All the resources keys associated with this peer
     */
    @Override
    public ArrayList<KADAddress> getKeys() {
        return new ArrayList<>(resourcesDict.keySet());
    }

    /**
     * @return All the resources values associated with this peer
     */
    @Override
    public ArrayList<RV> getValues() {
        return new ArrayList<>(resourcesDict.values());
    }


}

package com.eis.networklibrary.kademlia;

import com.eis.smslibrary.SMSPeer;
import com.eis.smslibrary.exceptions.InvalidTelephoneNumberException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.PriorityQueue;

/**
 * Collection used to hold the first k-nodes closest to a given address
 */
public class ClosestPQ extends PriorityQueue<ClosestPQ.SMSFindNodeKADPeer> {

    /**
     * Sets the comparator for the priority queue
     *
     * @param comparator contains the target address
     */
    public ClosestPQ(SMSKADPeer.KADComparator comparator, ArrayList<SMSKADPeer> collection) {
        super(comparator);
        Collection<SMSFindNodeKADPeer> internalCollection = new ArrayList<>();
        for(int i=0;i<collection.size();i++){
            internalCollection.add(new SMSFindNodeKADPeer(collection.get(i)));
        }
        addAll(internalCollection);
    }

    /**
     * Adds a {@link SMSKADPeer} object to the collection
     *
     * @param smskadPeer to add
     */
    public void add(SMSKADPeer smskadPeer) {
        super.add(new SMSFindNodeKADPeer(smskadPeer));
        if (size() > SMSDistributedNetworkDictionary.BUCKET_SIZE)
            remove(toArray()[size() - 1]);
    }

    /**
     * Inner class that extends {@link SMSKADPeer} and contains an extra flag, true if this object has being called, false otherwise
     */
    public class SMSFindNodeKADPeer extends SMSKADPeer {
        private boolean called = false;

        public SMSFindNodeKADPeer(String telephoneNumber) throws InvalidTelephoneNumberException {
            super(telephoneNumber);
        }

        public SMSFindNodeKADPeer(SMSPeer smsPeer) {
            super(smsPeer);
        }

        public SMSFindNodeKADPeer(SMSKADPeer smskadPeer) {
            super(smskadPeer.getAddress());
        }

        /**
         * @return the "called" variable
         */
        public boolean isCalled() {
            return called;
        }

        /**
         * Sets the "called" variable value
         *
         * @param state to assign to the "called" variable
         */
        public void setCalled(boolean state) {
            this.called = state;
        }
    }

}

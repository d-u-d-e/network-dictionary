package com.eis.networklibrary.kademlia;

import java.util.ArrayList;
import java.util.Collections;

import static com.eis.networklibrary.kademlia.SMSKADPeer.SMSKADComparator;
import static com.eis.networklibrary.kademlia.SMSDistributedNetworkDictionary.KADEMLIA_K;

/**
 * Priority queue used to keep track of the first k-nodes closer to a given address. Besides this, for each address X it stores
 * a Boolean flag, set to true only if X has been queried (by the network manager class). To accomplish this, it makes use of
 * {@link MutablePair} objects.
 * A comparator for kad addresses must be passed as first argument to the constructor.
 * This queue is bounded, meaning it can keep only KADEMLIA_K objects. When the queue is full, adding an object with
 * least priority will have no effect. Adding an object already present also has no effect.
 * size() and getAllPeers() are self-explanatory.
 * @author Marco Mariotto
 */
class ClosestPQ {

    private ArrayList<MutablePair<SMSKADPeer, Boolean>> pairs;
    private SMSKADComparator comparator;

    /**
     *
     * @param comparator used to define a order between addresses
     * @param nodes known addresses which are immediately compared and put in the queue.
     */
    ClosestPQ(SMSKADComparator comparator, ArrayList<SMSKADPeer> nodes) {
        if(nodes != null && !nodes.isEmpty()){
            Collections.sort(nodes, comparator);
            pairs = new ArrayList<>();
            for (SMSKADPeer p : nodes.subList(0, Math.min(KADEMLIA_K, nodes.size())))
                pairs.add(new MutablePair<>(p, false));
        }
        this.comparator = comparator;
    }

    /**
     *
     * @param i index of the element in the queue to be retrieved
     * @return the pair associated with index i
     */
    MutablePair<SMSKADPeer, Boolean> get(int i){
        return pairs.get(i);
    }

    /**
     * This queue is bounded, meaning it can keep only KADEMLIA_K objects. When the queue is full, adding an object with
     * least priority has no effect. Adding an object already present also has no effect.
     * @param pair to be added
     */
    void add(MutablePair<SMSKADPeer, Boolean> pair) {
        for (int i = 0; i < pairs.size(); i++) { //insertion sort
            int comp = comparator.compare(pair.first, pairs.get(i).first);
            if (comp == 0) return; //peer is already in the queue
            else if (comp < 0) {
                pairs.add(i, pair);
                if (pairs.size() > KADEMLIA_K)
                    pairs.remove(KADEMLIA_K - 1); //delete the last element if this queue has more than KADEMLIA_K elements
                break;
            }
        }
        if (pairs.size() < KADEMLIA_K)
            pairs.add(pair);
    }

    void add(SMSKADPeer peer, boolean b) {
        add(new MutablePair<>(peer, b));
    }

    int size() {
        return pairs.size();
    }

    SMSKADPeer[] getAllPeers() {
        SMSKADPeer[] peers = new SMSKADPeer[pairs.size()];
        for (int i = 0; i < pairs.size(); i++)
            peers[i] = pairs.get(i).first;
        return peers;
    }
}

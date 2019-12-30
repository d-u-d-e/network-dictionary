package com.eis.networklibrary.kademlia;

import java.util.ArrayList;
import java.util.Collections;

import static com.eis.networklibrary.kademlia.SMSKADPeer.SMSKADComparator;
import static com.eis.networklibrary.kademlia.SMSDistributedNetworkDictionary.BUCKET_SIZE;

/**
 * Priority queue used to keep track of the first k-nodes closer to {@code target}.
 * A lower priority means that a node is further from {@code target}; for each address X it stores
 * a Boolean flag, set to true only if X has been queried (by the network manager class). To accomplish this, it makes use of
 * {@link MutablePair} objects.
 * This queue is bounded, meaning it can keep only <i>BUCKET_SIZE</i> objects. When the queue is full, adding an object with
 * least priority will have no effect. Adding an object already present also has no effect.
 * @author Marco Mariotto
 */
class ClosestPQ {

    final private ArrayList<MutablePair<SMSKADPeer, Boolean>> pairs;
    final private SMSKADComparator comparator;

    /**
     *
     * @param target A kad address used to define an order between addresses.
     *               The node at the head is the current closest node to {@code target}
     *               according to the XOR metric.
     * @param nodes known addresses which are immediately compared and put in the queue.
     */
    ClosestPQ(KADAddress target, ArrayList<SMSKADPeer> nodes) {
        pairs = new ArrayList<>();
        this.comparator = new SMSKADComparator(target);

        if(nodes != null && !nodes.isEmpty()){
            Collections.sort(nodes, comparator);
            for (SMSKADPeer p : nodes.subList(0, Math.min(BUCKET_SIZE, nodes.size())))
                pairs.add(new MutablePair<>(p, false));
        }
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
     * Adds a pair (SMSKADPeer, Boolean) to the queue according to its priority.
     * This queue is bounded, meaning it can keep only KADEMLIA_K objects. When the queue is full, adding an object with
     * least priority has no effect. Adding an object already present also has no effect.
     * @param pair to be added
     */
    void add(MutablePair<SMSKADPeer, Boolean> pair) {
        for (int i = 0; i < pairs.size(); i++) { //insertion sort
            int comp = comparator.compare(pair.first, pairs.get(i).first);
            if(comp == 0) return;
            else if (comp < 0) {
                pairs.add(i, pair);
                if (pairs.size() > BUCKET_SIZE)
                    pairs.remove(BUCKET_SIZE - 1); //delete the last element if this queue has more than KADEMLIA_K elements
                return;
            }
        }
        if (pairs.size() < BUCKET_SIZE)
            pairs.add(pair);
    }


    /**
     * Adds a pair (SMSKADPeer peer, Boolean b) to the queue according to its priority.
     * @param peer to be added
     * @param b flag set only if the node has been queried.
     */
    void add(SMSKADPeer peer, boolean b) {
        add(new MutablePair<>(peer, b));
    }

    /**
     *
     * @return the current size of the queue. Maximum size is <i>KADEMLIA_K</i>
     */
    int size() {
        return pairs.size();
    }

    /**
     *
     * @return an array of all SMSKADPeers in the queue.
     */
    SMSKADPeer[] getAllPeers() {
        SMSKADPeer[] peers = new SMSKADPeer[pairs.size()];
        for (int i = 0; i < pairs.size(); i++)
            peers[i] = pairs.get(i).first;
        return peers;
    }
}

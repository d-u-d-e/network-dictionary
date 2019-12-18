package com.eis.networklibrary.kademlia;

import java.util.ArrayList;
import java.util.Collections;

import static com.eis.networklibrary.kademlia.SMSKADPeer.SMSKADComparator;
import static com.eis.networklibrary.kademlia.SMSDistributedNetworkDictionary.KADEMLIA_K;

/**
 * Collection used to keep the first k-nodes closest to a given address
 */
public class ClosestPQ {

    static class MutablePair<F, S> {
        F first;
        S second;

        MutablePair(F f, S s) {
            first = f;
            second = s;
        }
    }

    private ArrayList<MutablePair<SMSKADPeer, Boolean>> mutablePairs;
    SMSKADComparator comparator;

    ClosestPQ(SMSKADComparator comparator, ArrayList<SMSKADPeer> nodes) {
        Collections.sort(nodes, comparator);
        mutablePairs = new ArrayList<>();
        for (SMSKADPeer p : nodes.subList(0, Math.min(KADEMLIA_K, nodes.size())))
            mutablePairs.add(new MutablePair<>(p, false));
        this.comparator = comparator;
    }

    MutablePair<SMSKADPeer, Boolean> get(int index) {
        return mutablePairs.get(index);
    }

    void add(MutablePair<SMSKADPeer, Boolean> pair) {
        for (int i = 0; i < mutablePairs.size(); i++) {
            int comp = comparator.compare(pair.first, mutablePairs.get(i).first);
            if (comp == 0) return; //peer is already in the queue
            else if (comp < 0) { //insertion sort
                mutablePairs.add(i, pair);
                if (mutablePairs.size() > KADEMLIA_K)
                    mutablePairs.remove(KADEMLIA_K - 1); //delete the last element if this queue has more than KADEMLIA_K elements
                break;
            }
        }
        if (mutablePairs.size() < KADEMLIA_K)
            mutablePairs.add(pair);
    }

    void add(SMSKADPeer peer, boolean b) {
        add(new MutablePair<>(peer, b));
    }

    int size() {
        return mutablePairs.size();
    }

    SMSKADPeer[] getAllPeers() {
        SMSKADPeer[] peers = new SMSKADPeer[mutablePairs.size()];
        for (int i = 0; i < mutablePairs.size(); i++)
            peers[i] = mutablePairs.get(i).first;
        return peers;
    }
}

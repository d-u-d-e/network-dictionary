package com.eis.networklibrary.kademlia;

import android.util.Pair;
import java.util.ArrayList;
import java.util.Collections;

import static com.eis.networklibrary.kademlia.SMSKADPeer.SMSKADComparator;
import static com.eis.networklibrary.kademlia.SMSDistributedNetworkDictionary.KADEMLIA_K;

/**
 * Collection used to keep the first k-nodes closest to a given address
 */
public class ClosestPQ {

    static class MutablePair<F, S>
    {
        MutablePair(F f, S s){
            first = f;
            second = s;
        }
        F first;
        S second;
    }

    private ArrayList<MutablePair<SMSKADPeer, Boolean>> arr;
    SMSKADComparator cp;

    ClosestPQ(SMSKADComparator comparator, ArrayList<SMSKADPeer> nodes){
        Collections.sort(nodes, comparator);
        arr = new ArrayList<>();
        for(SMSKADPeer p: nodes.subList(0, Math.min(KADEMLIA_K, nodes.size())))
            arr.add(new MutablePair<>(p, false));
        cp = comparator;
    }

    MutablePair<SMSKADPeer, Boolean> get(int index){
        return  arr.get(index);
    }

    void add(SMSKADPeer peer){
        for(int i = 0; i < arr.size(); i++){
            int comp = cp.compare(peer, arr.get(i).first);
            if(comp == 0) return; //peer is already in the queue
            else if(comp < 0){ //insertion sort
                arr.add(i, new MutablePair<>(peer, false));
                if(arr.size() > KADEMLIA_K) arr.remove(KADEMLIA_K - 1); //delete the last element if this queue has more than KADEMLIA_K elements
                break;
            }
        }
        if(arr.size() < KADEMLIA_K)
            arr.add(new MutablePair<>(peer, false));
    }

    int size(){
        return arr.size();
    }

    SMSKADPeer[] getAllPeers(){
        SMSKADPeer[] peers = new SMSKADPeer[arr.size()];
        for(int i = 0; i < arr.size(); i++)
            peers[i] = arr.get(i).first;
        return peers;
    }
}

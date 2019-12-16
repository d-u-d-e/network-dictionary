package com.eis.networklibrary.kademlia;


import android.util.Pair;

import com.eis.communication.network.SerializableObject;
import com.eis.smslibrary.SMSPeer;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class handles the FindNode, FindValue, Reply and Join listeners
 * Uses KADAddress or SMSPeer as key to identify a specific listener
 * When a listener is triggered it is removed from the pending list
 *
 * @author Marco Tommasini
 */
public class SMSNetworkListenerHandler {

    private HashMap<KADAddress, FindNodeListener> findNodeListenerMap = new HashMap<>();
    private HashMap<KADAddress, FindValueListener> findValueListenerMap = new HashMap<>();
    private HashMap<SMSPeer, PingListener> pingListenerMap = new HashMap<>();

    //Only one joinListener for a list of SMSPeer
    private Pair<ArrayList<SMSPeer>, JoinListener> joinListenerPair;


    protected SMSNetworkListenerHandler() { }


    //region NodeListener
    /**
     * Registers a new NodeListener
     *
     * @param kadAddress    The address linked to the listener
     * @param listener      The listener to add to the pending list
     */
    protected void registerNodeListener(KADAddress kadAddress, FindNodeListener listener) {
        findNodeListenerMap.put(kadAddress, listener);
    }

    /**
     * Unregisters a NodeListener
     *
     * @param kadAddress    The address linked to the NodeListener
     * @return  The NodeListener removed
     */
    protected FindNodeListener removeNodeListener(KADAddress kadAddress) {
        return findNodeListenerMap.remove(kadAddress);
    }

    /**
     * Searches for the KADAddress in NodeListeners Map
     *
     * @param kadAddress    The address to find
     * @return  True if found
     */
    protected boolean isNodeAddressRegistered(KADAddress kadAddress) {
        return findNodeListenerMap.containsKey(kadAddress);
    }

    /**
     * Triggers onClosestNodeFound and removes the NodeListener
     *
     * @param kadAddress    The address linked to the NodeListener
     * @param peer          The peer found
     * @return  The NodeListener triggered
     */
    protected FindNodeListener triggerNodeFound(KADAddress kadAddress, SMSKADPeer peer) {
        FindNodeListener listener = removeNodeListener(kadAddress);
        if(listener != null)
            listener.onClosestNodeFound(peer);
        return listener;
    }
    //endregion


    //region ValueListener
    /**
     * Registers a new ValueListener
     *
     * @param kadAddress    The address linked to the listener
     * @param listener      The listener to add to the pending list
     */
    protected void registerValueListener(KADAddress kadAddress, FindValueListener listener) {
        findValueListenerMap.put(kadAddress, listener);
    }

    /**
     * Unregisters a ValueListener
     *
     * @param kadAddress    The address linked to the ValueListener
     * @return  The ValueListener removed
     */
    protected FindValueListener removeValueListener(KADAddress kadAddress) {
        return findValueListenerMap.remove(kadAddress);
    }

    /**
     * Searches for the KADAddress in ValueListeners Map
     *
     * @param kadAddress    The address to find
     * @return  True if found
     */
    protected boolean isValueAddressRegistered(KADAddress kadAddress) {
        return findValueListenerMap.containsKey(kadAddress);
    }

    /**
     * Triggers onValueFound and removes the ValueListener
     *
     * @param kadAddress    The address linked to the ValueListener
     * @param value         The peer found
     * @return  The ValueListener triggered
     */
    protected FindValueListener triggerValueFound(KADAddress kadAddress, SerializableObject value) {
        FindValueListener listener = removeValueListener(kadAddress);
        if(listener != null)
            listener.onValueFound(value);
        return listener;
    }

    /**
     * Triggers onValueNotFound and removes the ValueListener
     *
     * @param kadAddress    The address linked to the ValueListener
     * @return  The ValueListener triggered
     */
    protected FindValueListener triggerValueNotFound(KADAddress kadAddress) {
        FindValueListener listener = removeValueListener(kadAddress);
        if(listener != null)
            listener.onValueNotFound();
        return listener;
    }
    //endregion


    //region PingListener
    /**
     * Registers a new PingListener
     *
     * @param peer    The peer linked to the listener
     * @param listener      The listener to add to the pending list
     */
    protected void registerPingListener(SMSPeer peer, PingListener listener) {
        pingListenerMap.put(peer, listener);
    }

    /**
     * Unregisters a PingListener
     *
     * @param peer    The SMSPeer linked to the PingListener
     * @return  The PingListener removed
     */
    protected PingListener removePingListener(SMSPeer peer) {
        return pingListenerMap.remove(peer);
    }

    /**
     * Searches for the SMSPeer in PingListener Map
     *
     * @param peer    The SMSPeer to find
     * @return  True if found
     */
    protected boolean isPingPeerRegistered(SMSPeer peer){
        return pingListenerMap.containsKey(peer);
    }

    /**
     * Triggers onPingReply and removes the PingListener
     *
     * @param peer  The SMSPeer that replied
     * @return  The PingListener triggered
     */
    protected PingListener triggerPingReply(SMSPeer peer) {
        PingListener listener = removePingListener(peer);
        if(listener != null)
            listener.onPingReply(peer);
        return listener;
    }
    //endregion


    //region JoinListener

    /**
     * Sets the joinListener in use
     * 
     * @param joinListener  The joinListener in use
     */
    protected void setJoinListener(JoinListener joinListener) {
        joinListenerPair = new Pair<>(new ArrayList<SMSPeer>(), joinListener);
    }

    /**
     * Adds a new SMSPeer to pending list
     * Should set the joinListener first
     *
     * @param peer   The SMSPeer to add
     */
    protected void addSMSPeerToJoinProposal(SMSPeer peer) {
        joinListenerPair.first.add(peer);
    }

    /**
     * Remove a SMSPeer from pending list
     * Should set the joinListener first
     *
     * @param peer    The SMSPeer to remove
     * @return  True if removed successfully
     */
    protected boolean removeJoinListener(SMSPeer peer) {
        return joinListenerPair.first.remove(peer);
    }

    /**
     * Searches for the SMSPeer in the pending list
     * Should set the joinListener first
     *
     * @param peer    The SMSPeer to find
     * @return  True if found
     */
    protected boolean isSMSPeerInJoinProposal(SMSPeer peer) {
        return joinListenerPair.first.contains(peer);
    }

    /**
     * Triggers onJoinProposal and removes the SMSPeer from pending list
     * Should set the joinListener first
     *
     * @param peer          The SMSPeer found
     * @return  The JoinListener triggered
     */
    protected boolean triggerJoinProposal(SMSPeer peer) {
        boolean success = removeJoinListener(peer);
        if(success)
            joinListenerPair.second.onJoinProposal(peer);
        return success;
    }
    //endregion
}

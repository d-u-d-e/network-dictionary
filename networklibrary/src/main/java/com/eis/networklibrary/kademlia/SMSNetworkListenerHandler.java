package com.eis.networklibrary.kademlia;


import android.util.Pair;

import com.eis.communication.network.FindNodeListener;
import com.eis.communication.network.FindValueListener;
import com.eis.communication.network.JoinListener;
import com.eis.communication.network.PingListener;
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

    private HashMap<KADAddress, FindNodeListener<SMSKADPeer>> findNodeListenerMap = new HashMap<>();
    private HashMap<KADAddress, FindValueListener<SerializableObject>> findValueListenerMap = new HashMap<>();
    private HashMap<SMSPeer, PingListener> pingListenerMap = new HashMap<>();

    //Only one joinListener for a list of SMSPeer
    private Pair<ArrayList<KADInvitation>, JoinListener> joinListenerPair;

    //*******************************************************************************************

    /**
     * Registers a new NodeListener
     *
     * @param kadAddress The address linked to the listener
     * @param listener   The listener to add to the pending list
     */
    protected void registerNodeListener(KADAddress kadAddress, FindNodeListener listener) {
        findNodeListenerMap.put(kadAddress, listener);
    }

    /**
     * Searches for the KADAddress in NodeListeners Map
     *
     * @param kadAddress The address to find
     * @return True if found
     */
    protected boolean isNodeAddressRegistered(KADAddress kadAddress) {
        return findNodeListenerMap.containsKey(kadAddress);
    }

    /**
     * Triggers onClosestNodeFound and removes the NodeListener
     *
     * @param kadAddress The address linked to the NodeListener
     * @param peer       The peer found
     * @return The NodeListener triggered
     */
    protected void triggerKNodesFound(KADAddress kadAddress, SMSKADPeer[] peer) {
        FindNodeListener listener = findNodeListenerMap.remove(kadAddress);
        if (listener != null)
            listener.OnKClosestNodesFound(peer);
    }

    //*******************************************************************************************

    /**
     * Registers a new ValueListener
     *
     * @param kadAddress The address linked to the listener
     * @param listener   The listener to add to the pending list
     */
    protected void registerValueListener(KADAddress kadAddress, FindValueListener listener) {
        findValueListenerMap.put(kadAddress, listener);
    }

    /**
     * Searches for the KADAddress in ValueListeners Map
     *
     * @param kadAddress The address to find
     * @return True if found
     */
    protected boolean isValueAddressRegistered(KADAddress kadAddress) {
        return findValueListenerMap.containsKey(kadAddress);
    }

    /**
     * Triggers onValueFound and removes the ValueListener
     *
     * @param kadAddress The address linked to the ValueListener
     * @param value      The peer found
     * @return The ValueListener triggered
     */
    protected void triggerValueFound(KADAddress kadAddress, SerializableObject value) {
        FindValueListener listener = findValueListenerMap.remove(kadAddress);
        if (listener != null)
            listener.onValueFound(value);
    }

    /**
     * Triggers onValueNotFound and removes the ValueListener
     *
     * @param kadAddress The address linked to the ValueListener
     * @return The ValueListener triggered
     */
    protected void triggerValueNotFound(KADAddress kadAddress) {
        FindValueListener listener = findValueListenerMap.remove(kadAddress);
        if (listener != null)
            listener.onValueNotFound();
    }

    //*******************************************************************************************

    /**
     * Registers a new PingListener
     *
     * @param peer     The peer linked to the listener
     * @param listener The listener to add to the pending list
     */
    protected void registerPingListener(SMSPeer peer, PingListener listener) {
        pingListenerMap.put(peer, listener);
    }

    /**
     * Triggers onPingReply and removes the PingListener
     *
     * @param peer The SMSPeer that replied
     * @return The PingListener triggered
     */
    protected void triggerPingReply(SMSPeer peer) {
        PingListener listener = pingListenerMap.remove(peer);
        if (listener != null)
            listener.onPingReply(peer);
    }

    //*******************************************************************************************

    /**
     * Sets the joinListener in use
     *
     * @param joinListener The joinListener in use
     */
    protected void setJoinListener(JoinListener joinListener) {
        joinListenerPair = new Pair<>(new ArrayList<KADInvitation>(), joinListener);
    }

    /**
     * Adds a new SMSPeer to pending list
     * Should set the joinListener first
     *
     * @param peer The SMSPeer to add
     */
    protected void addToInvitedList(SMSPeer peer) {
        //TODO: Riguardare come sono stati gestiti gli inviti e i listener. @LucaCrema
        //joinListenerPair.first.add(peer);
    }

    /**
     * Searches for the SMSPeer in the pending list
     * Should set the joinListener first
     *
     * @param invitation The invitation to join a network.
     * @return True if found, false otherwise.
     */
    protected boolean isInvitationInJoinProposals(KADInvitation invitation) {
        return joinListenerPair.first.contains(invitation);
    }

    /**
     * Triggers onJoinProposal and removes the SMSPeer from pending list
     * Should set the joinListener first
     *
     * @param invitation The invitation to join a network.
     * @return The JoinListener triggered.
     */
    protected void triggerJoinProposal(KADInvitation invitation) {
        boolean success = joinListenerPair.first.remove(invitation);
        if (success)
            joinListenerPair.second.onJoinProposal(invitation);
    }
    //*******************************************************************************************
}

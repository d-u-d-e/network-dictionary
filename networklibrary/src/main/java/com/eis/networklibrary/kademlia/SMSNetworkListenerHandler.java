package com.eis.networklibrary.kademlia;


import com.eis.communication.network.SerializableObject;
import com.eis.smslibrary.SMSPeer;

import java.util.HashMap;

/**
 * This class handles the FindNode, FindValue and Join listeners
 * Uses KADAddress as key to identify a specific listener
 * When a listener is triggered it is removed from the waiting list
 *
 * @author Marco Tommasini
 */
public class SMSNetworkListenerHandler {

    private HashMap<KADAddress, FindNodeListener> findNodeListenerMap = new HashMap<>();
    private HashMap<KADAddress, FindValueListener> findValueListenerMap = new HashMap<>();
    private HashMap<KADAddress, JoinListener> joinListenerMap = new HashMap<>();


    //region NodeListener
    /**
     * Registers a new NodeListener
     *
     * @param kadAddress    the address linked to the listener
     * @param listener      the listener to add to the waiting list
     */
    protected void registerNodeListener(KADAddress kadAddress, FindNodeListener listener) {
        findNodeListenerMap.put(kadAddress, listener);
    }

    /**
     * Unregisters a NodeListener
     *
     * @param kadAddress    the address linked to the NodeListener
     * @return  the NodeListener removed
     */
    protected FindNodeListener removeNodeListener(KADAddress kadAddress) {
        return findNodeListenerMap.remove(kadAddress);
    }

    /**
     * Searches for the KADAddress in NodeListeners Map
     *
     * @param kadAddress    the address to find
     * @return  true if found
     */
    protected boolean isNodeAddressRegistered(KADAddress kadAddress) {
        return findNodeListenerMap.containsKey(kadAddress);
    }

    /**
     * Triggers onClosestNodeFound and removes the NodeListener
     *
     * @param kadAddress    the address linked to the NodeListener
     * @param peer          the peer found
     * @return  the NodeListener triggered
     */
    protected FindNodeListener triggerNodeListener(KADAddress kadAddress, SMSKADPeer peer) {
        FindNodeListener listener = findNodeListenerMap.remove(kadAddress);
        if(listener != null)
            listener.onClosestNodeFound(peer);
        return listener;
    }
    //endregion


    //region ValueListener
    /**
     * Registers a new ValueListener
     *
     * @param kadAddress    the address linked to the listener
     * @param listener      the listener to add to the waiting list
     */
    protected void registerValueListener(KADAddress kadAddress, FindValueListener listener) {
        findValueListenerMap.put(kadAddress, listener);
    }

    /**
     * Unregisters a ValueListener
     *
     * @param kadAddress    the address linked to the ValueListener
     * @return  the ValueListener removed
     */
    protected FindValueListener removeValueListener(KADAddress kadAddress) {
        return findValueListenerMap.remove(kadAddress);
    }

    /**
     * Searches for the KADAddress in ValueListeners Map
     *
     * @param kadAddress    the address to find
     * @return  true if found
     */
    protected boolean isValueAddressRegistered(KADAddress kadAddress) {
        return findValueListenerMap.containsKey(kadAddress);
    }

    /**
     * Triggers onValueFound and removes the ValueListener
     *
     * @param kadAddress    the address linked to the ValueListener
     * @param value         the peer found
     * @return  the ValueListener triggered
     */
    protected FindValueListener triggerValueListener(KADAddress kadAddress, SerializableObject value) {
        FindValueListener listener = findValueListenerMap.remove(kadAddress);
        if(listener != null)
            listener.onValueFound(value);
        return listener;
    }
    //endregion


    //region JoinListener
    /**
     * Registers a new JoinListener
     *
     * @param kadAddress    the address linked to the listener
     * @param listener      the listener to add to the waiting list
     */
    protected void registerJoinListener(KADAddress kadAddress, JoinListener listener) {
        joinListenerMap.put(kadAddress, listener);
    }

    /**
     * Unregisters a JoinListener
     *
     * @param kadAddress    the address linked to the JoinListener
     * @return  the JoinListener removed
     */
    protected JoinListener removeJoinListener(KADAddress kadAddress) {
        return joinListenerMap.remove(kadAddress);
    }

    /**
     * Searches for the KADAddress in JoinListener Map
     *
     * @param kadAddress    the address to find
     * @return  true if found
     */
    protected boolean isJoinAddressRegistered(KADAddress kadAddress) {
        return joinListenerMap.containsKey(kadAddress);
    }

    /**
     * Triggers onJoinProposal and removes the JoinListener
     *
     * @param kadAddress    the address linked to the JoinListener
     * @param peer          the peer found
     * @return  the JoinListener triggered
     */
    protected JoinListener triggerJoinListener(KADAddress kadAddress, SMSPeer peer) {
        JoinListener listener = joinListenerMap.remove(kadAddress);
        if(listener != null)
            listener.onJoinProposal(peer);
        return listener;
    }
    //endregion
}

package com.eis.networklibrary.kademlia;



import com.eis.communication.network.FindNodeListener;
import com.eis.communication.network.FindValueListener;
import com.eis.communication.network.PingListener;
import com.eis.communication.network.SerializableObject;
import com.eis.smslibrary.SMSPeer;

import java.util.HashMap;

/**
 * This class handles the FindNode, FindValue and Ping listeners
 * Uses KADAddress or SMSPeer as key to identify a specific listener
 * When a listener is triggered it is removed from the pending list
 *
 * @author Marco Tommasini
 */
public class SMSNetworkListenerHandler {

    private HashMap<KADAddress, FindNodeListener<SMSKADPeer>> findNodeListenerMap = new HashMap<>();
    private HashMap<KADAddress, FindValueListener<SerializableObject>> findValueListenerMap = new HashMap<>();
    private HashMap<SMSPeer, PingListener> pingListenerMap = new HashMap<>();

    //*******************************************************************************************

    /**
     * Registers a new NodeListener
     *
     * @param kadAddress The address linked to the listener
     * @param listener   The listener to add to the pending list
     * @return  The previous value corresponding to the key, null otherwise
     */
    protected FindNodeListener<SMSKADPeer> registerNodeListener(KADAddress kadAddress, FindNodeListener<SMSKADPeer> listener) {
        return findNodeListenerMap.put(kadAddress, listener);
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
     * @param kadAddress    The address linked to the NodeListener
     * @param peers         The peers found
     */
    protected void triggerKNodesFound(KADAddress kadAddress, SMSKADPeer[] peers) {
        FindNodeListener<SMSKADPeer> listener = findNodeListenerMap.remove(kadAddress);
        if (listener != null)
            listener.OnKClosestNodesFound(peers);
    }

    //*******************************************************************************************

    /**
     * Registers a new ValueListener
     *
     * @param kadAddress The address linked to the listener
     * @param listener   The listener to add to the pending list
     * @return  The previous value corresponding to the key, null otherwise
     */
    protected FindValueListener<SerializableObject> registerValueListener(KADAddress kadAddress, FindValueListener<SerializableObject> listener) {
        return findValueListenerMap.put(kadAddress, listener);
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
     * @param value      The value found
     */
    protected void triggerValueFound(KADAddress kadAddress, SerializableObject value) {
        FindValueListener<SerializableObject> listener = findValueListenerMap.remove(kadAddress);
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
        FindValueListener<SerializableObject> listener = findValueListenerMap.remove(kadAddress);
        if (listener != null)
            listener.onValueNotFound();
    }

    //*******************************************************************************************

    /**
     * Registers a new PingListener
     *
     * @param peer     The peer linked to the listener
     * @param listener The listener to add to the pending list
     * @return  The previous value corresponding to the key, null otherwise
     */
    protected PingListener registerPingListener(SMSPeer peer, PingListener listener) {
        return pingListenerMap.put(peer, listener);
    }

    /**
     * Triggers onPingReply and removes the PingListener
     *
     * @param peer The SMSPeer that replied
     */
    protected void triggerPingReply(SMSPeer peer) {
        //TODO check if it timed out
        PingListener listener = pingListenerMap.remove(peer);
        if (listener != null)
            listener.onPingReply(peer);
    }

    //*******************************************************************************************
}

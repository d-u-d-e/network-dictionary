package com.eis.networklibrary.kademlia;



import com.eis.communication.network.FindNodeListener;
import com.eis.communication.network.FindValueListener;
import com.eis.communication.network.PingListener;
import com.eis.communication.network.SerializableObject;
import com.eis.smslibrary.SMSPeer;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class handles the findNode, findValue and ping listeners
 * Uses KADAddress or SMSPeer as key to identify a specific listener
 * When a listener is triggered it is removed from the pending list
 *
 * @author Marco Tommasini
 */
public class SMSNetworkListenerHandler {

    private HashMap<KADAddress, FindNodeListener<SMSKADPeer>> findNodeListenerMap = new HashMap<>();
    private HashMap<KADAddress, FindValueListener<SerializableObject>> findValueListenerMap = new HashMap<>();
    private HashMap<SMSPeer, PingListener> pingListenerMap = new HashMap<>();

    private Timer pingTimeOutTimer = new Timer();
    private final static int COUNTDOWN_MILLIS = 30*1000;  //30 seconds

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
     */
    protected void triggerValueNotFound(KADAddress kadAddress) {
        FindValueListener<SerializableObject> listener = findValueListenerMap.remove(kadAddress);
        if (listener != null)
            listener.onValueNotFound();
    }

    //*******************************************************************************************

    /**
     * Registers a new PingListener
     * Starts a countDown that calls onPingTimedOut when finished
     *
     * @param peer     The peer linked to the listener
     * @param listener The listener to add to the pending list
     * @return  The previous value corresponding to the key, null otherwise
     */
    protected PingListener registerPingListener(final SMSPeer peer, PingListener listener) {
        pingTimeOutTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                triggerPingTimedOut(peer);
            }
        }, COUNTDOWN_MILLIS);
        return pingListenerMap.put(peer, listener);
    }

    /**
     * Triggers onPingReply and removes the PingListener
     * Finishes the pingTimeOutTimer if onPingReply is called
     *
     * @param peer The SMSPeer that replied
     */
    protected void triggerPingReply(SMSPeer peer) {
        PingListener listener = pingListenerMap.remove(peer);
        if (listener != null) {
            listener.onPingReply(peer);
            pingTimeOutTimer.cancel();
        }
    }

    /**
     * Triggers onPingTimedOut and removes the PingListener
     *
     * @param peer The SMSPeer that did not replied
     */
    private void triggerPingTimedOut(SMSPeer peer) {
        PingListener listener = pingListenerMap.remove(peer);
        if (listener != null)
            listener.onPingTimedOut(peer);
    }

    //*******************************************************************************************
}

package com.eis.networklibrary.kademlia;



import android.os.CountDownTimer;

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

    private CountDownTimer timer;
    private final static int SECONDINMILLIS = 1000;
    private final static int COUNTDOWNINMILLIS = 30*SECONDINMILLIS;

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
    protected FindNodeListener<SMSKADPeer> triggerKNodesFound(KADAddress kadAddress, SMSKADPeer[] peers) {
        FindNodeListener<SMSKADPeer> listener = findNodeListenerMap.remove(kadAddress);
        if (listener != null)
            listener.OnKClosestNodesFound(peers);
        return listener;
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
     * @return The ValueListener triggered
     */
    protected FindValueListener<SerializableObject> triggerValueFound(KADAddress kadAddress, SerializableObject value) {
        FindValueListener<SerializableObject> listener = findValueListenerMap.remove(kadAddress);
        if (listener != null)
            listener.onValueFound(value);
        return listener;
    }

    /**
     * Triggers onValueNotFound and removes the ValueListener
     *
     * @param kadAddress The address linked to the ValueListener
     * @return The ValueListener triggered
     */
    protected FindValueListener<SerializableObject> triggerValueNotFound(KADAddress kadAddress) {
        FindValueListener<SerializableObject> listener = findValueListenerMap.remove(kadAddress);
        if (listener != null)
            listener.onValueNotFound();
        return listener;
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
        timer = new CountDownTimer(COUNTDOWNINMILLIS, SECONDINMILLIS) {
            @Override
            public void onTick(long l) { }

            @Override
            public void onFinish() {
                triggerPingTimedOut(peer);
            }
        }.start();
        return pingListenerMap.put(peer, listener);
    }

    /**
     * Triggers onPingReply and removes the PingListener
     * Finishes the timer if onPingReply is called
     *
     * @param peer The SMSPeer that replied
     * @return  The PingListener triggered
     *          Null if there is no listener or the time is up
     */
    protected PingListener triggerPingReply(SMSPeer peer) {
        PingListener listener = pingListenerMap.remove(peer);
        if (listener != null) {
            listener.onPingReply(peer);
            timer.onFinish();
        }
        return listener;
    }

    /**
     * Triggers onPingTimedOut and removes the PingListener
     *
     * @param peer The SMSPeer that did not replied
     * @return  The PingListener triggered
     *          Null if there is no listener or onPingReply was called before this
     */
    private PingListener triggerPingTimedOut(SMSPeer peer) {
        PingListener listener = pingListenerMap.remove(peer);
        if (listener != null)
            listener.onPingTimedOut(peer);
        return listener;
    }

    //*******************************************************************************************
}

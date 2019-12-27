package com.eis.networklibrary.kademlia;



import com.eis.communication.network.FindNodeListener;
import com.eis.communication.network.FindValueListener;
import com.eis.communication.network.PingListener;
import com.eis.communication.network.SerializableObject;
import com.eis.smslibrary.SMSPeer;

import java.util.HashMap;
import java.util.ListIterator;
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

    final private HashMap<KADAddress, FindNodeListener<SMSKADPeer>> findNodeListenerMap = new HashMap<>();
    final private HashMap<KADAddress, FindValueListener<SerializableObject>> findValueListenerMap = new HashMap<>();
    final private HashMap<SMSPeer, PingListener> pingListenerMap = new HashMap<>();

    final private HashMap<KADAddress, Timer> requestTimers = new HashMap<>();
    final private HashMap<SMSPeer, Timer> pingTimers = new HashMap<>();

    private final static int PING_TIMEOUT_MILLIS = 30*1000;  //30 seconds

    //*******************************************************************************************

    /**
     * Registers a new NodeListener
     *
     * @param kadAddress The address linked to the listener
     * @param listener   The listener to add to the pending list
     * @param maxWaiting Maximum milliseconds to wait before unregistering the listener. After this timeout, listener.onFindNodesTimedOut() is called.
     *                   Note that if maxWaiting is 0, no timeout is set.
     */
    synchronized protected void registerNodeListener(final KADAddress kadAddress, FindNodeListener<SMSKADPeer> listener, int maxWaiting) {
        findNodeListenerMap.put(kadAddress, listener);
        if(maxWaiting > 0){
            Timer t = new Timer();
            requestTimers.put(kadAddress, t);
            t.schedule(new TimerTask() {
                @Override
                public void run() {
                    FindNodeListener<SMSKADPeer> l;
                    synchronized (findNodeListenerMap){
                        l = findNodeListenerMap.remove(kadAddress);
                    }
                    if(l != null)
                        l.onFindTimedOut(); //l == listener
                    synchronized (requestTimers){
                        requestTimers.remove(kadAddress);
                    }
                }
            }, maxWaiting);
        }
    }

    /**
     * Searches for the KADAddress in NodeListeners Map
     *
     * @param kadAddress The address to find
     * @return True if found
     */
    synchronized protected boolean isNodeAddressRegistered(KADAddress kadAddress) {
        return findNodeListenerMap.containsKey(kadAddress);
    }

    /**
     * Triggers onClosestNodeFound and removes the NodeListener
     *
     * @param kadAddress    The address linked to the NodeListener
     * @param peers         The peers found
     */
    protected void triggerKNodesFound(KADAddress kadAddress, SMSKADPeer[] peers) {
        FindNodeListener<SMSKADPeer> listener;
        synchronized (findNodeListenerMap){
            listener = findNodeListenerMap.remove(kadAddress);
        }
        if (listener != null){ //request not timed out
            synchronized (requestTimers){
                Timer t = requestTimers.remove(kadAddress);
                if(t != null)
                    t.cancel();
            }
            listener.OnKClosestNodesFound(peers);
        }
    }

    //*******************************************************************************************

    /**
     * Registers a new ValueListener
     *
     * @param kadAddress The address linked to the listener
     * @param listener   The listener to add to the pending list
     * @param maxWaiting Maximum milliseconds to wait before unregistering the listener. After this timeout, listener.onFindValueTimedOut() is called.
     *                   Note that if maxWaiting is 0, no timeout is set.
     */
    synchronized protected void registerValueListener(final KADAddress kadAddress, FindValueListener<SerializableObject> listener, int maxWaiting) {
        findValueListenerMap.put(kadAddress, listener);
        if(maxWaiting > 0){
            Timer t = new Timer();
            requestTimers.put(kadAddress, t);
            t.schedule(new TimerTask() {
                @Override
                public void run() {
                    FindValueListener<SerializableObject> l;
                    synchronized (findValueListenerMap){
                        l = findValueListenerMap.remove(kadAddress);
                    }
                    if(l != null)
                        l.onFindValueTimedOut(); //l == listener
                    synchronized (requestTimers){
                        requestTimers.remove(kadAddress);
                    }
                }
            }, maxWaiting);
        }
    }

    /**
     * Searches for the KADAddress in ValueListeners Map
     *
     * @param kadAddress The address to find
     * @return True if found
     */
    synchronized protected boolean isValueAddressRegistered(KADAddress kadAddress) {
        return findValueListenerMap.containsKey(kadAddress);
    }

    /**
     * Triggers onValueFound and removes the ValueListener
     *
     * @param kadAddress The address linked to the ValueListener
     * @param value      The value found
     */
    protected void triggerValueFound(KADAddress kadAddress, SerializableObject value) {
        FindValueListener<SerializableObject> listener;
        synchronized (findValueListenerMap){
            listener = findValueListenerMap.remove(kadAddress);
        }
        if (listener != null){ //request not timed out
            synchronized (requestTimers){
                Timer t = requestTimers.remove(kadAddress);
                if(t != null)
                    t.cancel();
            }
            listener.onValueFound(value);
        }
    }

    /**
     * Triggers onValueNotFound and removes the ValueListener
     *
     * @param kadAddress The address linked to the ValueListener
     */
    protected void triggerValueNotFound(KADAddress kadAddress) {
        FindValueListener<SerializableObject> listener;
        synchronized (findValueListenerMap){
            listener = findValueListenerMap.remove(kadAddress);
        }
        if (listener != null){ //request not timed out
            synchronized (requestTimers){
                Timer t = requestTimers.remove(kadAddress);
                if(t != null)
                    t.cancel();
            }
            listener.onValueNotFound();
        }
    }

    //*******************************************************************************************

    /**
     * Registers a new PingListener
     * Starts a countDown that calls onPingTimedOut when finished
     *
     * @param peer     The peer linked to the listener
     * @param listener The listener to add to the pending list
     */
    synchronized protected void registerPingListener(final SMSPeer peer, final PingListener listener) {
        Timer t = new Timer();
        pingTimers.put(peer, t);
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                PingListener l;
                synchronized (pingListenerMap){
                    l = pingListenerMap.remove(peer);
                }
                if(l != null)
                    listener.onPingTimedOut(peer);
                synchronized (pingTimers){
                    pingTimers.remove(peer);
                }
            }
        }, PING_TIMEOUT_MILLIS);
    }

    /**
     * Triggers onPingReply and removes the PingListener
     * Finishes the pingTimeOutTimer if onPingReply is called
     *
     * @param peer The SMSPeer that replied
     */
    protected void triggerPingReply(SMSPeer peer) {
        PingListener listener;
        synchronized (pingListenerMap){
            listener = pingListenerMap.remove(peer);
        }

        if (listener != null) {
            listener.onPingReply(peer);
            synchronized (pingTimers){
                Timer t = pingTimers.remove(peer);
                if(t != null)
                    t.cancel();
            }
        }
    }
}

//*******************************************************************************************

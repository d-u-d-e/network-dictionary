package com.eis.networklibrary.kademlia;


import androidx.annotation.NonNull;

import com.eis.communication.network.FindNodeListener;
import com.eis.communication.network.FindValueListener;
import com.eis.communication.network.PingListener;
import com.eis.communication.network.SerializableObject;
import com.eis.smslibrary.SMSPeer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SMSNetworkListenerHandlerTest {

    private SMSNetworkListenerHandler listenerHandler;
    private KADAddress KAD_ADDRESS_1;
    private SMSKADPeer[] KADPEERS;
    private SerializableObject SER_OBJECT;
    private SMSPeer SMSPEER_1;
    final private String PHONE_NUMBER = "+391111111111";
    final private String PHONE_NUMBER2 = "+391111111112";
    private FindNodeListener<SMSKADPeer> NODE_LISTENER_1, NODE_LISTENER_2;
    private FindValueListener<SerializableObject> VALUE_LISTENER_1, VALUE_LISTENER_2;
    private PingListener PING_LISTENER_1, PING_LISTENER_2;
    private int MAXWAITING = 30*1000;

    @Before
    public void init() {
        listenerHandler = new SMSNetworkListenerHandler();
        KAD_ADDRESS_1 = new KADAddress((new byte[]{106, 97, 118, 97, 32, 105, 115, 32, 111, 107}));
        KADPEERS = new SMSKADPeer[]{new SMSKADPeer(PHONE_NUMBER), new SMSKADPeer(PHONE_NUMBER2)};
        SER_OBJECT = new SerializableObject() {
            @Override
            public boolean equals(Object other) {
                return true; //returns always true because in this case we are using only one SerializableObject
            }

            @NonNull
            @Override
            public String toString() { return null; }
        };
        SMSPEER_1 = new SMSPeer(PHONE_NUMBER);
        NODE_LISTENER_1 = peer -> { };
        NODE_LISTENER_2 = peer -> { };

        VALUE_LISTENER_1 = new FindValueListener<SerializableObject>() {
            @Override
            public void onValueFound(SerializableObject value) { }

            @Override
            public void onValueNotFound() { }
        };
        VALUE_LISTENER_2 = new FindValueListener<SerializableObject>() {
            @Override
            public void onValueFound(SerializableObject value) { }

            @Override
            public void onValueNotFound() { }
        };

        PING_LISTENER_1 = new PingListener() {
            @Override
            public void onPingReply(SMSPeer peer) { }

            @Override
            public void onPingTimedOut(SMSPeer peer) { }
        };
        PING_LISTENER_2 = new PingListener() {
            @Override
            public void onPingReply(SMSPeer peer) { }

            @Override
            public void onPingTimedOut(SMSPeer peer) { }
        };
    }

    private void reset_instance() {
        listenerHandler = new SMSNetworkListenerHandler();
    }

    //*******************************************************************************************

    @Test
    public void register_nodeListener_test() {
        reset_instance();
        listenerHandler.registerFindNodesRequest(KAD_ADDRESS_1, NODE_LISTENER_1, MAXWAITING);
        Assert.assertEquals(listenerHandler.getNodeListener(KAD_ADDRESS_1), NODE_LISTENER_1);
    }

    /**
     * Tests if by updating a value returns the previous one
     */
    @Test
    public void update_nodeListener_test() {
        reset_instance();
        listenerHandler.registerFindNodesRequest(KAD_ADDRESS_1, NODE_LISTENER_1, MAXWAITING);
        listenerHandler.registerFindNodesRequest(KAD_ADDRESS_1, NODE_LISTENER_2, MAXWAITING);
        Assert.assertEquals(listenerHandler.getNodeListener(KAD_ADDRESS_1), NODE_LISTENER_2);

    }

    @Test
    public void trigger_k_nodes_found_test() {
        reset_instance();
        NODE_LISTENER_1 = peer -> Assert.assertArrayEquals(peer, KADPEERS);
        listenerHandler.registerFindNodesRequest(KAD_ADDRESS_1, NODE_LISTENER_1, MAXWAITING);
        listenerHandler.triggerKNodesFound(KAD_ADDRESS_1, KADPEERS);
    }

    //*******************************************************************************************

    @Test
    public void register_valueListener_test() {
        reset_instance();
        listenerHandler.registerFindValueRequest(KAD_ADDRESS_1, VALUE_LISTENER_1, MAXWAITING);
        Assert.assertEquals(listenerHandler.getValueListener(KAD_ADDRESS_1), VALUE_LISTENER_1);
    }

    /**
     * Tests if by updating a value returns the previous one
     */
    @Test
    public void update_valueListener_test() {
        reset_instance();
        listenerHandler.registerFindValueRequest(KAD_ADDRESS_1, VALUE_LISTENER_1, MAXWAITING);
        listenerHandler.registerFindValueRequest(KAD_ADDRESS_1, VALUE_LISTENER_2, MAXWAITING);
        Assert.assertEquals(listenerHandler.getValueListener(KAD_ADDRESS_1), VALUE_LISTENER_2);
    }

    @Test
    public void trigger_value_found_test() {
        reset_instance();
        VALUE_LISTENER_1 = new FindValueListener<SerializableObject>() {
            @Override
            public void onValueFound(SerializableObject value) {
                Assert.assertEquals(value, SER_OBJECT);
            }

            @Override
            public void onValueNotFound() { }
        };
        listenerHandler.registerFindValueRequest(KAD_ADDRESS_1, VALUE_LISTENER_1, MAXWAITING);
        listenerHandler.triggerValueFound(KAD_ADDRESS_1, SER_OBJECT);
    }

    @Test
    public void trigger_value_not_found_test() {
        reset_instance();
        VALUE_LISTENER_1 = new FindValueListener<SerializableObject>() {
            @Override
            public void onValueFound(SerializableObject value) { }

            @Override
            public void onValueNotFound() {
                Assert.assertTrue(true);
            }
        };
        listenerHandler.registerFindValueRequest(KAD_ADDRESS_1, VALUE_LISTENER_1, MAXWAITING);
        listenerHandler.triggerValueNotFound(KAD_ADDRESS_1);
    }

    //*******************************************************************************************

    @Test
    public void register_pingListener_test() {
        reset_instance();
        listenerHandler.registerPingRequest(SMSPEER_1, PING_LISTENER_1);
        Assert.assertEquals(listenerHandler.getPingListener(SMSPEER_1), PING_LISTENER_1);
    }

    /**
     * Tests if by updating a value returns the previous one
     */
    @Test
    public void update_pingListener_test() {
        reset_instance();
        listenerHandler.registerPingRequest(SMSPEER_1, PING_LISTENER_1);
        listenerHandler.registerPingRequest(SMSPEER_1, PING_LISTENER_2);
        Assert.assertEquals(listenerHandler.getPingListener(SMSPEER_1), PING_LISTENER_2);
    }

    @Test
    public void trigger_ping_reply_test() {
        reset_instance();
        PING_LISTENER_1 = new PingListener() {
            @Override
            public void onPingReply(SMSPeer peer) {
                Assert.assertEquals(peer, SMSPEER_1);
            }

            @Override
            public void onPingTimedOut(SMSPeer peer) { }
        };
        listenerHandler.registerPingRequest(SMSPEER_1, PING_LISTENER_1);
        listenerHandler.triggerPingReply(SMSPEER_1);
    }

    //TODO: use powermock for testing listeners

    //*******************************************************************************************

}

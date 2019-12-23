package com.eis.networklibrary.kademlia;


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
    private KADAddress KAD_ADDRESS_1, KAD_ADDRESS_2;
    private SMSKADPeer[] KADPEERS;
    private SerializableObject SER_OBJECT;
    private SMSPeer SMSPEER_1, SMSPEER_2;
    final private String PHONE_NUMBER = "+391111111111";
    final private String PHONE_NUMBER2 = "+391111111112";
    private FindNodeListener<SMSKADPeer> NODE_LISTENER_1, NODE_LISTENER_2;
    private FindValueListener<SerializableObject> VALUE_LISTENER_1, VALUE_LISTENER_2;
    private PingListener PING_LISTENER_1, PING_LISTENRE_2;

    @Before
    public void init() {
        listenerHandler = new SMSNetworkListenerHandler();
        KAD_ADDRESS_1 = new KADAddress((new byte[]{106, 97, 118, 97, 32, 105, 115, 32, 111, 107}));
        KAD_ADDRESS_2 = new KADAddress((new byte[]{8, 97, 118, 97, 32, 105, 115, 32, 111, 107}));
        KADPEERS = new SMSKADPeer[]{new SMSKADPeer(PHONE_NUMBER), new SMSKADPeer(PHONE_NUMBER2)};
        SMSPEER_1 = new SMSPeer(PHONE_NUMBER);
        SMSPEER_2 = new SMSPeer(PHONE_NUMBER2);
        NODE_LISTENER_1 = new FindNodeListener<SMSKADPeer>() {
            @Override
            public void OnKClosestNodesFound(SMSKADPeer[] peer) { }
        };
        NODE_LISTENER_2 = new FindNodeListener<SMSKADPeer>() {
            @Override
            public void OnKClosestNodesFound(SMSKADPeer[] peer) { }
        };

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
        PING_LISTENRE_2 = new PingListener() {
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
        Assert.assertNull(listenerHandler.registerNodeListener(KAD_ADDRESS_1, NODE_LISTENER_1));
    }

    /**
     * Tests if by updating a value returns the previous one
     */
    @Test
    public void update_nodeListener_test() {
        reset_instance();
        listenerHandler.registerNodeListener(KAD_ADDRESS_1, NODE_LISTENER_1);
        Assert.assertEquals(listenerHandler.registerNodeListener(KAD_ADDRESS_1, NODE_LISTENER_2), NODE_LISTENER_1);
    }

    @Test
    public void is_nodeAddress_registered_test() {
        reset_instance();
        listenerHandler.registerNodeListener(KAD_ADDRESS_1, NODE_LISTENER_1);
        Assert.assertTrue(listenerHandler.isNodeAddressRegistered(KAD_ADDRESS_1));
    }

    @Test
    public void is_nodeAddress_not_registered_test() {
        reset_instance();
        listenerHandler.registerNodeListener(KAD_ADDRESS_1, NODE_LISTENER_1);
        Assert.assertFalse(listenerHandler.isNodeAddressRegistered(KAD_ADDRESS_2));
    }

    @Test
    public void trigger_k_nodes_found_test() {
        reset_instance();
        NODE_LISTENER_1 = new FindNodeListener<SMSKADPeer>() {
            @Override
            public void OnKClosestNodesFound(SMSKADPeer[] peer) {
                Assert.assertArrayEquals(peer, KADPEERS);
            }
        };
        listenerHandler.registerNodeListener(KAD_ADDRESS_1, NODE_LISTENER_1);
        listenerHandler.triggerKNodesFound(KAD_ADDRESS_1, KADPEERS);
    }

    //*******************************************************************************************

    @Test
    public void register_valueListener_test() {
        reset_instance();
        Assert.assertNull(listenerHandler.registerValueListener(KAD_ADDRESS_1, VALUE_LISTENER_1));
    }

    /**
     * Tests if by updating a value returns the previous one
     */
    @Test
    public void update_valueListener_test() {
        reset_instance();
        listenerHandler.registerValueListener(KAD_ADDRESS_1, VALUE_LISTENER_1);
        Assert.assertEquals(listenerHandler.registerValueListener(KAD_ADDRESS_1, VALUE_LISTENER_2), VALUE_LISTENER_1);
    }

    @Test
    public void is_valueAddress_registered_test() {
        reset_instance();
        listenerHandler.registerValueListener(KAD_ADDRESS_1, VALUE_LISTENER_1);
        Assert.assertTrue(listenerHandler.isValueAddressRegistered(KAD_ADDRESS_1));
    }

    @Test
    public void is_valueAddress_not_registered_test() {
        reset_instance();
        listenerHandler.registerValueListener(KAD_ADDRESS_1, VALUE_LISTENER_1);
        Assert.assertFalse(listenerHandler.isValueAddressRegistered(KAD_ADDRESS_2));
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
        listenerHandler.registerValueListener(KAD_ADDRESS_1, VALUE_LISTENER_1);
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
        listenerHandler.registerValueListener(KAD_ADDRESS_1, VALUE_LISTENER_1);
        listenerHandler.triggerValueNotFound(KAD_ADDRESS_1);
    }

    //*******************************************************************************************

    @Test
    public void register_pingListener_test() {
        reset_instance();
        Assert.assertNull(listenerHandler.registerPingListener(SMSPEER_1, PING_LISTENER_1));
    }

    /**
     * Tests if by updating a value returns the previous one
     */
    @Test
    public void update_pingListener_test() {
        reset_instance();
        listenerHandler.registerPingListener(SMSPEER_1, PING_LISTENER_1);
        Assert.assertEquals(listenerHandler.registerPingListener(SMSPEER_1, PING_LISTENRE_2), PING_LISTENER_1);
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
        listenerHandler.registerPingListener(SMSPEER_1, PING_LISTENER_1);
        listenerHandler.triggerPingReply(SMSPEER_1);
    }

    @Test
    public void trigger_ping_timeout_test() {
        reset_instance();
        PING_LISTENER_1 = new PingListener() {
            @Override
            public void onPingReply(SMSPeer peer) {

            }

            @Override
            public void onPingTimedOut(SMSPeer peer) {

            }
        };
        listenerHandler.registerPingListener(SMSPEER_1, PING_LISTENER_1);
        //TODO: triggerOnPingTimeOut creating a countdown
        //TODO: mock CountDownTimer
    }

    //*******************************************************************************************

}

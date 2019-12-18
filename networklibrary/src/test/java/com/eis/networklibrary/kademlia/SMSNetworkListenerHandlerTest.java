package com.eis.networklibrary.kademlia;


import com.eis.communication.Peer;
import com.eis.communication.network.FindNodeListener;
import com.eis.communication.network.FindValueListener;
import com.eis.communication.network.PingListener;
import com.eis.communication.network.SerializableObject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SMSNetworkListenerHandlerTest {

    private SMSNetworkListenerHandler listenerHandler;
    private KADAddress KAD_ADDRESS_1, KAD_ADDRESS_2;
    private SMSKADPeer[] PEERS;
    final private String PHONE_NUMBER = "+391111111111";
    final private String PHONE_NUMBER2 = "+391111111112";
    private FindNodeListener<SMSKADPeer> NODE_LISTENER_1, NODE_LISTENER_2;
    private FindValueListener<SerializableObject> VALUE_LISTENER;
    private PingListener PING_LISTENER;

    @Before
    public void init() {
        listenerHandler = new SMSNetworkListenerHandler();
        KAD_ADDRESS_1 = new KADAddress((new byte[]{106, 97, 118, 97, 32, 105, 115, 32, 111, 107}));
        KAD_ADDRESS_2 = new KADAddress((new byte[]{8, 97, 118, 97, 32, 105, 115, 32, 111, 107}));
        PEERS = new SMSKADPeer[]{new SMSKADPeer(PHONE_NUMBER), new SMSKADPeer(PHONE_NUMBER2)};
        NODE_LISTENER_1 = new FindNodeListener<SMSKADPeer>() {
            @Override
            public void OnKClosestNodesFound(SMSKADPeer[] peer) {

            }
        };
        NODE_LISTENER_2 = new FindNodeListener<SMSKADPeer>() {
            @Override
            public void OnKClosestNodesFound(SMSKADPeer[] peer) {

            }
        };
    }

    private void reset_instance() {
        listenerHandler = new SMSNetworkListenerHandler();
    }

    //*******************************************************************************************

    @Test
    public void register_nodeListener_test() {
        Assert.assertNull(listenerHandler.registerNodeListener(KAD_ADDRESS_1, NODE_LISTENER_1));
        reset_instance();
    }

    /**
     * Tests if by updating a value returns the previous one
     */
    @Test
    public void update_nodeListener_test() {
        listenerHandler.registerNodeListener(KAD_ADDRESS_1, NODE_LISTENER_1);
        Assert.assertEquals(listenerHandler.registerNodeListener(KAD_ADDRESS_1, NODE_LISTENER_2), NODE_LISTENER_1);
        reset_instance();
    }

    @Test
    public void is_nodeAddress_registered_test() {
        listenerHandler.registerNodeListener(KAD_ADDRESS_1, NODE_LISTENER_1);
        Assert.assertTrue(listenerHandler.isNodeAddressRegistered(KAD_ADDRESS_1));
        reset_instance();
    }

    @Test
    public void is_nodeAddress_not_registered_test() {
        listenerHandler.registerNodeListener(KAD_ADDRESS_1, NODE_LISTENER_1);
        Assert.assertFalse(listenerHandler.isNodeAddressRegistered(KAD_ADDRESS_2));
        reset_instance();
    }

    @Test
    public void trigger_k_nodes_found_test() {
        NODE_LISTENER_1 = new FindNodeListener<SMSKADPeer>() {
            @Override
            public void OnKClosestNodesFound(SMSKADPeer[] peer) {
                Assert.assertArrayEquals(peer, PEERS);
            }
        };
        listenerHandler.registerNodeListener(KAD_ADDRESS_1, NODE_LISTENER_1);
        listenerHandler.triggerKNodesFound(KAD_ADDRESS_1, PEERS);
        reset_instance();
    }

    //*******************************************************************************************
}

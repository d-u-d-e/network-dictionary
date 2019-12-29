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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Testing: listener registration
 *          listener updating
 *          listener callback
 * I choose not to test timeout callbacks because I think it is not UnitTest's duty
 *
 * @author Marco Tommasini
 *
 * Ready for code review from Barca and Bortoletti
 */

public class SMSNetworkListenerHandlerTest {

    private KADAddress KAD_ADDRESS_1;
    private SMSKADPeer[] KADPEERS;
    private SerializableObject SER_OBJECT;
    private SMSPeer SMSPEER_1;
    final private String PHONE_NUMBER = "+391111111111";
    final private String PHONE_NUMBER2 = "+391111111112";
    private int MAXWAITING = 30 * 1000;

    /**
     * Using Mockito to test listeners' callbacks
     */
    @Mock
    private FindNodeListener<SMSKADPeer> NODE_LISTENER_1, NODE_LISTENER_2;
    @Mock
    private FindValueListener<SerializableObject> VALUE_LISTENER_1, VALUE_LISTENER_2;
    @Mock
    private PingListener PING_LISTENER_1, PING_LISTENER_2;

    @InjectMocks
    private SMSNetworkListenerHandler listenerHandler = new SMSNetworkListenerHandler();

    @Before
    public void init() {

        MockitoAnnotations.initMocks(this);

        KAD_ADDRESS_1 = new KADAddress((new byte[]{106, 97, 118, 97, 32, 105, 115, 32, 111, 107}));
        KADPEERS = new SMSKADPeer[]{new SMSKADPeer(PHONE_NUMBER), new SMSKADPeer(PHONE_NUMBER2)};
        SER_OBJECT = new SerializableObject() {
            @Override
            public boolean equals(Object other) {
                return false;
            }

            @NonNull
            @Override
            public String toString() {
                return null;
            }
        };
        SMSPEER_1 = new SMSPeer(PHONE_NUMBER);
    }

    /**
     * Must call this before any test to ensure that the instance is brand new
     */
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
        listenerHandler.registerFindNodesRequest(KAD_ADDRESS_1, NODE_LISTENER_1, MAXWAITING);
        listenerHandler.triggerKNodesFound(KAD_ADDRESS_1, KADPEERS);
        verify(NODE_LISTENER_1, times(1)).OnKClosestNodesFound(KADPEERS);
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
        listenerHandler.registerFindValueRequest(KAD_ADDRESS_1, VALUE_LISTENER_1, MAXWAITING);
        listenerHandler.triggerValueFound(KAD_ADDRESS_1, SER_OBJECT);
        verify(VALUE_LISTENER_1, times(1)).onValueFound(SER_OBJECT);
    }

    @Test
    public void trigger_value_not_found_test() {
        reset_instance();
        listenerHandler.registerFindValueRequest(KAD_ADDRESS_1, VALUE_LISTENER_1, MAXWAITING);
        listenerHandler.triggerValueNotFound(KAD_ADDRESS_1);
        verify(VALUE_LISTENER_1, times(1)).onValueNotFound();
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
        listenerHandler.registerPingRequest(SMSPEER_1, PING_LISTENER_1);
        listenerHandler.triggerPingReply(SMSPEER_1);
        verify(PING_LISTENER_1, times(1)).onPingReply(SMSPEER_1);
    }

    //*******************************************************************************************

}

package com.eis.networklibrary.kademlia;

import androidx.annotation.NonNull;

import com.eis.communication.network.SerializableObject;
import com.eis.smslibrary.SMSPeer;

import org.junit.Before;

public class SMSNetworkManagerTest {

    private static final String NETWORK_NAME = "test net";
    private static final SMSPeer MYSELF = new SMSPeer("+393457090735");
    private final SerializableObjectParser DEFAULT_PARSER = new SerializableObjectParser() {
        @Override
        public SerializableObject deSerialize(String content) {
            return new TestSerializableObject(content);
        }
    };

    private SMSNetworkManager testNetMan;

    @Before
    private void setup(){
        testNetMan = SMSNetworkManager.getInstance();
        testNetMan.setup(NETWORK_NAME, MYSELF, DEFAULT_PARSER);
    }

    class TestSerializableObject extends SerializableObject {

        String mString;

        public TestSerializableObject(String string){
            this.mString = string;
        }
        @Override
        public boolean equals(Object other) {
            return mString.equals(other);
        }

        @NonNull
        @Override
        public String toString() {
            return mString;
        }
    }

}
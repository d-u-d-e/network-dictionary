package com.eis.networklibrary.kademlia;

import com.eis.communication.network.SerializableObject;

public abstract class SerializableObjectParser {

    /**
     * Serializes objects into strings
     *
     * @param object input object
     * @return a string representing uniquely the object
     */
    public String serialize(SerializableObject object) {
        return object.toString();
    }

    /**
     * Parses strings back into objects
     *
     * @param content input string
     * @return the object de-serialized
     */
    public abstract SerializableObject deSerialize(String content);

}

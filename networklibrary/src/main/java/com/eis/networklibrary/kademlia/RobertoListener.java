package com.eis.networklibrary.kademlia;

import com.eis.communication.network.SerializableObject;

public interface RobertoListener {

    /**
     * Called when a resource is found
     *
     * @param resource the resource requested
     */
    void onValueReceived(SerializableObject resource);

    /**
     * Called when value is not found
     */
    void onValueNotFound();
}

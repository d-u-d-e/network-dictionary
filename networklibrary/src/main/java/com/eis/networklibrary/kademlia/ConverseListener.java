package com.eis.networklibrary.kademlia;

import com.eis.communication.network.SerializableObject;

public interface ConverseListener {

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

    void onNodeFound(SMSKADPeer peer);
}

package com.eis.networklibrary.kademlia;

import com.eis.communication.network.SerializableObject;


/**
 * A listener waiting for a {@code VALUE_FOUND} or {@code VALUE_NOT_FOUND} reply
 *
 * */
public interface FindValueListener
{
    void onValueNotFound();
    void onValueFound(SerializableObject value);
}

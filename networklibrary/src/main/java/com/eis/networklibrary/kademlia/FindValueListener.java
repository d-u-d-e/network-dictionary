package com.eis.networklibrary.kademlia;

import com.eis.communication.network.SerializableObject;

public interface FindValueListener {

    void onValueNotFound();
    void onValueFound(SerializableObject value);
}

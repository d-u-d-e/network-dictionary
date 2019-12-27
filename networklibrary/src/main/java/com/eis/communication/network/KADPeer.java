package com.eis.communication.network;

import com.eis.networklibrary.kademlia.KADAddress;

/**
 * Represents a user of the Kademlia network
 */
public interface KADPeer{
    /**
     * @return The peer's {@link KADAddress}
     */
    KADAddress getNetworkAddress();
}

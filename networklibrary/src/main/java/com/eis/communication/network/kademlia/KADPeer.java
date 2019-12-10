package com.eis.communication.network.kademlia;

import com.eis.networklibrary.kademlia.KADAddress;

/**
 * Represents a user of the distributed network
 *
 * @author Luca Crema
 * @author Marco Mariotto
 * @since 10/12/2019
 */
public interface KADPeer {

    /**
     * Calculates the peer unique address for Kademlia network
     *
     * @return The peer's kad address
     */
    KADAddress getNetworkAddress();

}

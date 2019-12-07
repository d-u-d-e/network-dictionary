package com.network.networklibrary.distributed;

import com.eis.communication.Peer;
import com.eis.smslibrary.SMSPeer;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.BitSet;

/**
 * This class manages the peer address, as hash of his phone number
 *
 * @author Luca Crema, Alessandra Tonin, Mariotto Marco
 */
public class KADPeerAddress implements Peer<byte[]> {

    public static final String HASH_ALGORITHM = "SHA-256";
    public static final int BYTE_ADDRESS_LENGTH = 10;

    private byte[] address;

    /**
     * Constructor
     *
     * @param address a byte[] containing the peer address
     * @throws IllegalArgumentException
     */
    public KADPeerAddress(byte[] address) throws IllegalArgumentException
    {
        if (address.length != BYTE_ADDRESS_LENGTH)
            throw new IllegalArgumentException("Byte address should be " + BYTE_ADDRESS_LENGTH + " bytes long");
        this.address = address;
    }

    /**
     * Constructor
     *
     * @param phoneAddress a String containing the peer phone number
     * @throws NoSuchAlgorithmException
     */
    public KADPeerAddress(String phoneAddress)
    {
        try {
            MessageDigest digestAlgorithm = MessageDigest.getInstance(HASH_ALGORITHM);
            address = new byte[BYTE_ADDRESS_LENGTH];
            System.arraycopy(digestAlgorithm.digest(phoneAddress.getBytes(StandardCharsets.UTF_8)), 0, address, 0, BYTE_ADDRESS_LENGTH);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructor
     *
     * @param peer a network user
     */
    public KADPeerAddress(SMSPeer peer)
    {
        this(peer.getAddress());
    }

    @Override
    public byte[] getAddress() {
        return address;
    }

    /**
     * Calculates the first bit that differs starting from left
     *
     * @param otherAddress the address of another network user
     * @return the index of the first different bit between this address and otherAddress
     * or -1 if no such index exists (i.e. the two addresses are equal)
     */
    public int firstDifferentBit(KADPeerAddress otherAddress) {
        BitSet userBitSet = BitSet.valueOf(address);
        userBitSet.xor(BitSet.valueOf(otherAddress.getAddress()));
        return userBitSet.nextSetBit(0);
    }
}

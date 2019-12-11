package com.eis.networklibrary.kademlia;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.BitSet;

/**
 * This class manages the peer address, as hash of his phone number
 *
 * @author Luca Crema
 * @author Alessandra Tonin
 * @author Mariotto Marco
 * @since 10/12/2019
 */
public class KADAddress {

    public static final String HASH_ALGORITHM = "SHA-256";
    public static final int BYTE_ADDRESS_LENGTH = 10;

    protected byte[] address;

    /**
     * TODO
     *
     * @param address a byte array containing the object address
     * @throws IllegalArgumentException TODO
     */
    public KADAddress(byte[] address) throws IllegalArgumentException {
        if (address.length != BYTE_ADDRESS_LENGTH)
            throw new IllegalArgumentException("Byte address should be " + BYTE_ADDRESS_LENGTH + " bytes long");
        this.address = address;
    }

    /**
     * TODO
     *
     * @param objectKey A String containing the key identifier for the object
     */
    public KADAddress(String objectKey) {
        try {
            MessageDigest digestAlgorithm = MessageDigest.getInstance(HASH_ALGORITHM);
            address = new byte[BYTE_ADDRESS_LENGTH];
            byte[] hashedObject = digestAlgorithm.digest(objectKey.getBytes(StandardCharsets.UTF_8));
            System.arraycopy(hashedObject, 0, address, 0, BYTE_ADDRESS_LENGTH);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return a byte[] address
     */
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
    public int firstDifferentBit(KADAddress otherAddress) {
        BitSet userBitSet = BitSet.valueOf(address);
        userBitSet.xor(BitSet.valueOf(otherAddress.getAddress()));
        return userBitSet.nextSetBit(0);
    }
}

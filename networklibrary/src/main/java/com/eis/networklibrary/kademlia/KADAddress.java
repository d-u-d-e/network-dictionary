package com.eis.networklibrary.kademlia;

import androidx.annotation.NonNull;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * This class manages the peer address, as hash of his phone number
 *
 * @author Luca Crema
 * @author Alessandra Tonin
 * @author Mariotto Marco
 * @author Alberto Ursino
 * @since 10/12/2019
 */
public class KADAddress {

    public static final String HASH_ALGORITHM = "SHA-256";
    public static final int BYTE_ADDRESS_LENGTH = 10;
    public static final int BIT_LENGTH = Byte.SIZE * BYTE_ADDRESS_LENGTH;
    private static final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();

    /**
     * 10 byte long variable that represents the address of a resource or a user in the dictionary (big-endian)
     */
    protected byte[] address;

    /**
     * @param address a big-endian byte array containing the object address.
     * @throws IllegalArgumentException if address does not consist of {@link #BYTE_ADDRESS_LENGTH} bytes
     */
    public KADAddress(byte[] address) throws IllegalArgumentException {
        if (address.length != BYTE_ADDRESS_LENGTH)
            throw new IllegalArgumentException("Byte address should be " + BYTE_ADDRESS_LENGTH + " bytes long");
        this.address = address;
    }

    /**
     * @param objectKey A String containing the key identifier for the object. This method creates a hash for the string representation
     *                  of the object key (serialization). Not to be confused with {@link #fromHexString(String str)} which takes an hexadecimal equivalent
     *                  representation of this address
     */
    public KADAddress(String objectKey) {
        address = new byte[BYTE_ADDRESS_LENGTH];
        try {
            MessageDigest digestAlgorithm = MessageDigest.getInstance (HASH_ALGORITHM);
            byte[] hashedObject = digestAlgorithm.digest(objectKey.getBytes(StandardCharsets.UTF_8));
            System.arraycopy(hashedObject, 0, address, 0, BYTE_ADDRESS_LENGTH);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return the byte[] address
     */
    public byte[] getAddress() {
        return address;
    }



    /**
     * @param obj  the object being compared to <i>this</i>
     * @return     true if and only if {@code obj} is of the {@link KADAddress} type and has the same bytes as this address
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof KADAddress) {
            return Arrays.equals(address, ((KADAddress) obj).address);
        }
        return false;
    }
}

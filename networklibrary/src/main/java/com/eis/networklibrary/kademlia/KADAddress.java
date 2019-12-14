package com.eis.networklibrary.kademlia;

import androidx.annotation.NonNull;

import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;

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

    public KADAddress(BitSet bitset) throws IllegalArgumentException {
        address = new byte[BYTE_ADDRESS_LENGTH];
        byte[] arr = bitset.toByteArray(); //a little endian representation of the bitSet
        if (arr.length < BYTE_ADDRESS_LENGTH)
            throw new IllegalArgumentException("Byte address should be " + BYTE_ADDRESS_LENGTH + " bytes long");
        System.arraycopy(arr, 0, address, 0, BYTE_ADDRESS_LENGTH);
    }

    /**
     * @param objectKey A String containing the key identifier for the object
     */
    public KADAddress(String objectKey) {
        address = new byte[BYTE_ADDRESS_LENGTH];
        try {
            MessageDigest digestAlgorithm = MessageDigest.getInstance(HASH_ALGORITHM);
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


    /**
     * @param obj object being compared to
     * @return true if and only if obj is of KADAddress type and has the same bytes as this address
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof KADAddress) {
            return Arrays.equals(address, ((KADAddress) obj).address);
        }
        return false;
    }

    /**
     * Method used to verify which of the two nodes {@param a} and {@param b} is closest to a given {@param target}
     *
     * @param a      1st {@link KADAddress} object to compare
     * @param b      2nd {@link KADAddress} object to compare
     * @param target a KADAddress which is compared to a and b
     * @return a or b, whichever is closer to target according to XOR metric
     */
    static KADAddress closerToTarget(KADAddress a, KADAddress b, KADAddress target) {
        return new KADAddress(closerToTarget(BitSet.valueOf(a.getAddress()), BitSet.valueOf(b.getAddress()), BitSet.valueOf(target.getAddress())));
    }

    /**
     * Method used to verify which of the two nodes {@param a} and {@param b} is closest to a given {@param target}
     *
     * @param a      1st {@link BitSet} object to compare
     * @param b      2nd {@link BitSet} object to compare
     * @param target a bitSet which is compared to a and b
     * @return a or b, whichever is closer to target according to XOR metric
     */
    private static BitSet closerToTarget(BitSet a, BitSet b, BitSet target) {
        for (int i = 0; i < BIT_LENGTH; i++) { //BitSet returns a little-endian representation of a byte array
            boolean aBit = a.get(i);
            boolean bBit = b.get(i);
            if (aBit != bBit) {
                if (aBit == target.get(i)) return a;
                else return b;
            }
        }
        return a; //a == b
    }

    /**
     * @return The string hexadecimal representation of {@link #address}
     */
    @NonNull
    public String toString() { //
        char[] hexChars = new char[address.length * 2]; //two hex chars for each byte
        for (int i = 0; i < address.length; i++) { //for each byte
            //convert it to a non negative integer: to see why this works, note that & operator is defined only between integers or long ints (not between bytes)
            //address[i] is promoted to int (by extending the sign) and 0xFF is just 00 00 00 FF as int
            int v = address[i] & 0xFF;
            hexChars[i * 2] = HEX_DIGITS[v >>> 4]; //first hex char is at index v divided by 16
            hexChars[i * 2 + 1] = HEX_DIGITS[v & 0x0F]; //second hex char is at index specified by the second group of 4 bits
        }
        return new String(hexChars);
    }

    /**
     * @param str a valid hexadecimal representation of a KADAddress; str.length() must be BIT_LENGTH/4 bits long and even
     * @return the KADAddress having str as its hexadecimal representation
     */
    public static KADAddress fromHexString(String str) {
        int len = str.length();
        byte[] arr = new byte[len / 2];
        for (int i = 0; i < len - 1; i += 2)
            arr[i / 2] = (byte) ((Character.digit(str.charAt(i), 16) << 4) + Character.digit(str.charAt(i + 1), 16));
        return new KADAddress(arr);
    }

}

package com.network.communication;

import androidx.annotation.NonNull;

/**
 * A resource key or value has to be represented by a string in order to be sent by a sms.
 * Thus every user-defined key or value extends this class, which force the user to override equals()
 * and toString().
 * @author Marco Mariotto
 */
public abstract class SerializableObject {

    /**
     * Mandatory override for equals()
     *
     * @param other another object
     * @return true if the object represented is the same
     */
    public abstract boolean equals(Object other);

    /**
     * Mandatory override for toString()
     *
     * @return a string representing the state of the object
     */

    @NonNull
    public abstract String toString();

}

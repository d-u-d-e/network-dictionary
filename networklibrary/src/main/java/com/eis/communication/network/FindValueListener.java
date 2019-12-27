package com.eis.communication.network;

/**
 * Listener called to handle findValue requests
 */
public interface FindValueListener<V extends SerializableObject> {
    /**
     * Method called when the value is found.
     *
     * @param value the found value of the given key.
     */
    void onValueFound(V value);

    /**
     * Method called when it was not possible to find the value of the given key.
     */
    void onValueNotFound();

    /**
     * Method called when the find request timed out.
     */
    default void onFindValueTimedOut(){};
}

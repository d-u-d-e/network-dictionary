package com.eis.communication.network;

/**
 * Listener used for requests to obtain a value given a key in the network
 *
 * @author ?
 * @author Luca Crema
 * @since 16/12/2019
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

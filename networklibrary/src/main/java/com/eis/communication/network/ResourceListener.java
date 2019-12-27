package com.eis.communication.network;

/**
 * A listener that notifies the user if setResource or removeResource calls have been successful or not
 */
public interface ResourceListener {
    void onOperationSuccessful();
    void onOperationFailed();
}

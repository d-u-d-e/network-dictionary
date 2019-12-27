package com.eis.communication.network;

/**
 * A listener waiting for a completed {@code STORE} action
 */
public interface ResourceListener {
    void onOperationSuccessful();

    void onOperationFail();
}

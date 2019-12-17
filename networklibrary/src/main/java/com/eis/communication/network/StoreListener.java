package com.eis.communication.network;

/**
 * A listener waiting for a completed {@code STORE} action
 */
public interface StoreListener {
    void onStoreSuccess();
}

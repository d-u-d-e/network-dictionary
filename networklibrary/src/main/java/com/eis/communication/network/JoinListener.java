package com.eis.communication.network;

/**
 * Listener used to be notified when someone invites you to join a network
 *
 */
public interface JoinListener<I extends Invitation> {
    /**
     * Method called when someone invites you to join the network.
     * If you want to join you should call {@link NetworkManager#}
     *
     * @param invitation the invitation message.
     */
    void onJoinProposal(I invitation);
}

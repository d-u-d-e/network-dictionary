package com.network.networklibrary.replicated;

import com.eis.smslibrary.SMSHandler;
import com.eis.smslibrary.SMSMessage;
import com.eis.smslibrary.SMSPeer;
import com.network.communication.NetworkManager;
import com.network.communication.SerializableObject;

import java.util.ArrayList;

/**
 * This class is intended to be extended by the specific application. It is an implementation of NetworkManager.
 * Each SMS request has a format specified below. A node cannot inform each node in the network about his changes to
 * the local dictionary, for it will be expensive in terms of messages sent. Thus we ordered our dictionary by phone number, so that
 * each node has the same dictionary ordered in the same way: a node with index n in the dictionary will spread changes to only nodes numbered
 * 2n-req+N+1, 2n-req+N+2 (mod N) guaranteeing mutual exclusion, where N is the extension of the network and req is the requester,
 * the node who sent the first request (to put it in a simple way, n chooses the first nodes who are not expecting the message; for example, let N = 4;
 * if the requester is 0, then 0 will send to 1 and 2; 1 will send to 3. 2 knows it doesn't have to spread further. To see how this is done, check out
 * spread method)
 * This costs 2 messages for each node and delivery of information is logarithmic in N.
 *
 * @author Marco Mariotto, Alessandra Tonin
 */
public abstract class SMSAbstractNetworkManager implements NetworkManager<SMSPeer, SerializableObject, SerializableObject> {

    /**
     * SMS REQUESTS FORMATS
     * Join proposal:    "JP_%netName"
     * Add user:         "AU_%(requesterIndex)_%(peer)"          we include the whole peer, not only his address
     * Remove user:      "RU_%(requesterIndex)_%(address)"       address is the phone number of the user being removed
     * Add resource:     "AR_%(requesterIndex)_%(key)_%(value)"  we include the whole resource, key and value
     * Remove resource:  "RR_%(requesterIndex)_%(key)"           we only need the key to identify a resource
     * Don't spread:     "%(1)DS_%(2)"           inform the receiver to not spread this info, %(1) is one of {AU, RU, AR, RR},
     * %(2) can be peer, address, a <key, value> pair or key
     */
    static final String ADD_USER = "AU";
    static final String REMOVE_USER = "RU";
    static final String ADD_RESOURCE = "AR";
    static final String REMOVE_RESOURCE = "RR";
    static final String DO_NOT_SPREAD = "DS";
    static final String JOIN_AGREED = "JA";
    static final String JOIN_PROPOSAL = "JP";
    static final String SPLIT_CHAR = "_";

    private SMSReplicatedNetworkDictionary dict = new SMSReplicatedNetworkDictionary<>();

    //joinSent keeps track of JOIN_PROPOSAL requests still pending.
    private ArrayList<SMSPeer> joinSent = new ArrayList<>();
    protected String networkName;
    //mySelf is the current peer setting up the network
    protected SMSPeer mySelf;

    //manager makes use of SMSHandler to send requests
    private SMSHandler handler;

    /**
     * Sets up a new network
     *
     * @param handler     we set up a handler for sending requests
     * @param networkName of the network being created
     * @param mySelf      the current peer executing setup()
     */
    public void setup(SMSHandler handler, String networkName, SMSPeer mySelf) {
        this.handler = handler;
        this.networkName = networkName;
        this.mySelf = mySelf;
        dict.addUser(mySelf);
    }

    /**
     * Sends an invitation (JOIN_PROPOSAL request) to the specified peer
     *
     * @param peer who is asked to join the network
     */
    public void invite(SMSPeer peer) {
        SMSMessage invMsg = new SMSMessage(peer, JOIN_PROPOSAL + "_" + networkName);
        joinSent.add(peer);
        handler.sendMessage(invMsg);
    }

    /**
     * Informs every user in the network that the current application is disconnecting from the network
     * Sends a REMOVE_USER request, where user is mySelf
     */
    public void disconnect() {
        int myIndex = getMyIndex();
        spread(myIndex, REMOVE_USER + "_" + myIndex + "_" + mySelf.getAddress());
        dict = null;
        joinSent = null;
        handler = null;
    }

    /**
     * Sets a key-value resource for the local dictionary and spread this information to every user
     *
     * @param key   resource key
     * @param value resource value
     */
    public void setResource(SerializableObject key, SerializableObject value) {
        dict.setResource(key, value);
        int myIndex = getMyIndex();
        spread(myIndex, ADD_RESOURCE + "_" + myIndex + "_" + key.toString() + "_" + value.toString());
    }

    /**
     * Removes a key-value resource from the local dictionary and spread this information to every user
     *
     * @param key resource key
     */
    public void removeResource(SerializableObject key) {
        dict.removeResource(key);
        int myIndex = getMyIndex();
        spread(myIndex, REMOVE_RESOURCE + "_" + myIndex + "_" + key.toString());
    }

    /**
     * Spreads this string according to your current index n in the dictionary. For how nodes are chosen, read the method's comments.
     * For example, let N = 8. Suppose node 4 sends a REMOVE_RESOURCE request. We have the following tree, assuming formulas below:
     * <p>
     *              4
     *          5       6
     *       7    0   1   2
     *     3  4
     *   4
     * <p>
     * we clearly see that node 7 should stop after he has reached node 3, since node 4 is the requester. Similarly node 3 should not even attempt to
     * spread this request further. Now every node has been reached.
     *
     * @param requester the node which sends the request
     * @param text to be sent
     */
    private void spread(int requester, String text) {
        ArrayList<SMSPeer> users = dict.getAllUsers();
        int N = users.size();
        int myIndex = getMyIndex();
        /*
         * node n sends to req + 2(n-req) + 1 and req + 2(n-req) + 2, that is:
         * node 2n - req + 1 (+N) and 2n - req + 2 (+N) (mod N). If one of these nodes is the requester, we don't spread further.
         */

        int first = (2 * myIndex - requester + N + 1) % N;
        for (int i = 0; i < 2 && i + first != requester; i++) {
            SMSMessage msg = new SMSMessage(users.get(first + i), text);
            handler.sendMessage(msg);
        }
    }

    /**
     * Gets the index of the current peer
     *
     * @return the requested index as explained above, or -1 if not found
     */
    private int getMyIndex() {
        return dict.getAllUsers().indexOf(mySelf);
    }

    /**
     * It processes every request performing changes to the local dictionary. If otherwise specified (DO_NOT_SPREAD flag included)
     * spreads each request to his corresponding nodes according to the number rule previously stated.
     * When a JOIN_AGREED request has to be processed, we also send the whole dictionary to the new node who just joined.
     *
     * @param message containing the request to be processed
     */
    void processRequest(SMSMessage message) {
        String text = message.getData();
        String[] splitText = text.split("_");
        String request = splitText[0];
        SMSPeer sourcePeer = message.getPeer();
        int myIndex = getMyIndex();

        switch(request){
            case JOIN_AGREED: {
                if (joinSent.contains(sourcePeer)) {
                    spread(myIndex, ADD_USER + "_" + myIndex + "_" + sourcePeer.toString());
                    dict.addUser(sourcePeer);
                    joinSent.remove(sourcePeer);
                    //send the whole dictionary to sourcePeer
                    for (Object obj : dict.getKeys()) {
                        SerializableObject key = (SerializableObject) obj;
                        SMSMessage record = new SMSMessage(sourcePeer,
                                ADD_RESOURCE + DO_NOT_SPREAD +
                                        "_" + key.toString() + "_" + dict.getValue(key).toString());
                        handler.sendMessage(record);
                    }
                    for (Object obj : dict.getAllUsers()) {
                        SMSPeer peer = (SMSPeer) obj;
                        SMSMessage record = new SMSMessage(sourcePeer,
                                ADD_USER + DO_NOT_SPREAD + "_" + peer.toString());
                        handler.sendMessage(record);
                    }
                } else {
                    //ignore non-matching joins
                }
                break;
            }
            case ADD_USER: {
                int requester = Integer.parseInt(splitText[1]);
                SMSPeer p = new SMSPeer(splitText[2]);
                spread(requester, ADD_USER + "_" + requester + "_" + p.toString());
                dict.addUser(p);
                break;
            }
            case ADD_USER + DO_NOT_SPREAD: {
                SMSPeer p = new SMSPeer(splitText[1]);
                dict.addUser(p);
                break;
            }
            case REMOVE_USER: {
                int requester = Integer.parseInt(splitText[1]);
                SMSPeer p = new SMSPeer(splitText[2]); //we build a peer with no info, but his address is enough to find it in the list
                spread(requester, REMOVE_USER + "_" + requester + "_" + p.getAddress());
                dict.removeUser(p);
                break;
            }
            case ADD_RESOURCE: {
                int requester = Integer.parseInt(splitText[1]);
                spread(requester, ADD_RESOURCE + "_" + requester + "_" + splitText[2] + "_" + splitText[3]);
                dict.setResource(getKeyFromString(splitText[2]), getValueFromString(splitText[3]));
                break;
            }
            case ADD_RESOURCE + DO_NOT_SPREAD: {
                dict.setResource(getKeyFromString(splitText[1]), getValueFromString(splitText[2]));
                break;
            }
            case REMOVE_RESOURCE: {
                String key = splitText[2];
                int requester = Integer.parseInt(splitText[1]);
                spread(requester, REMOVE_RESOURCE + "_" + requester + "_" + key);
                dict.removeResource(getKeyFromString(key));
                break;
            }
            default: {
                throw new IllegalStateException("Should not have received this command");
            }
        }
    }

    /**
     * Construction of specific objects for resource keys cannot be done here. It is up to the application to override this method.
     *
     * @param key as string
     */
    protected abstract SerializableObject getKeyFromString(String key);

    /**
     * Construction of specific objects for resource values cannot be done here. It is up to the application to override this method.
     *
     * @param value as string
     */
    protected abstract SerializableObject getValueFromString(String value);
}

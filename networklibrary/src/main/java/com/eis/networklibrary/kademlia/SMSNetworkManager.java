package com.eis.networklibrary.kademlia;

import androidx.annotation.NonNull;

import com.eis.communication.network.NetworkManager;
import com.eis.communication.network.SerializableObject;
import com.eis.communication.network.kademlia.KADPeer;
import com.eis.smslibrary.SMSHandler;
import com.eis.smslibrary.SMSMessage;
import com.eis.smslibrary.SMSPeer;
import com.eis.smslibrary.listeners.SMSSentListener;

import java.util.ArrayList;

/**
 * Singleton class that handles the Kademlia network.
 * If you want to join a network you have to
 * 1. Setup the manager with SMSNetworkManager.getInstance().setup(...)
 * 2. Set a callback listener for events like join proposal with SMSNetworkManager.getInstance().setCallbackListener(...)
 *
 * @author Marco Mariotto
 * @author Alessandra Tonin
 * @author Alberto Ursino
 */
@SuppressWarnings("WeakerAccess")
public class SMSNetworkManager implements NetworkManager<SMSKADPeer, SerializableObject, SerializableObject> {

    protected static SMSNetworkManager instance;
    protected String networkName;
    protected SMSKADPeer mySelf;
    protected SerializableObjectParser keyParser;
    protected SerializableObjectParser valueParser;
    protected SMSNetworkCallbackListener callbackListener;
    protected char SPLIT_CHAR = '_';
    private SMSDistributedNetworkDictionary<SerializableObject> dict;
    //joinSent keeps track of JOIN_PROPOSAL requests still pending.
    private ArrayList<SMSPeer> joinSent = new ArrayList<>();
    private ReplyListener resourceListener;

    private SMSNetworkManager() {
        //Private because of singleton
    }

    protected static SMSNetworkManager getInstance() {
        if (instance == null)
            instance = new SMSNetworkManager();
        return instance;
    }

    /**
     * Sets up a new network
     *
     * @param networkName Name of the network being created
     * @param mySelf      The current peer executing setup()
     */
    public void setup(String networkName, SMSPeer mySelf, SerializableObjectParser keyParser, SerializableObjectParser valueParser) {
        this.networkName = networkName;
        this.mySelf = new SMSKADPeer(mySelf);
        dict = new SMSDistributedNetworkDictionary<>(new SMSKADPeer(mySelf));
        SMSHandler.getInstance().setReceivedListener(SMSNetworkListener.class);
        this.keyParser = keyParser;
        this.valueParser = valueParser;
    }

    public void setCallbackListener(SMSNetworkCallbackListener callbackListener) {
        this.callbackListener = callbackListener;
    }

    /**
     * Sends an invitation to the specified peer
     *
     * @param peer The peer of the user to invite
     */
    @Override
    public void invite(@NonNull final SMSKADPeer peer) {
        SMSCommandMapper.sendRequest(Request.JOIN_PROPOSAL, networkName, peer, new SMSSentListener() {
            @Override
            public void onSMSSent(SMSMessage message, SMSMessage.SentState sentState) {
                joinSent.add(peer);
            }
        });
    }

    /**
     * Inserts the {@link SMSMessage} peer in the network
     *
     * @param invitation The invitation message
     */
    public void join(SMSMessage invitation) {
        //TODO Take a look to this method
        //TODO: 1. Aggiungere chi ci ha invitato al nostro dizionario
        dict.addUser(new SMSKADPeer(invitation.getPeer()));
        //TODO: 2. Dirgli che ci vogliamo aggiungere alla rete

        //TODO: 3. Aspettare che ci mandi la lista di peer (va fatto in un altro metodo tipo onJoinAccomplished)
    }

    /**
     * Sets a (key, value) resource in the local dictionary: this is called only if a STORE message is received
     *
     * @param key   The resource key
     * @param value The resource value
     */
    @Override
    public void setResource(SerializableObject key, SerializableObject value) {
        //TODO Find the closest node and tell to it to store the (key, value) pair
        //TODO 1. Trovare il bucket in cui si trova la risorsa o, se non esiste, quello più vicino ad essa
        //TODO 2. Confrontare gli indirizzi degli utenti di quel bucket con quello della risorsa e prendere il più vicino
        //TODO 3. Rendere responsabile della risorsa l'utente trovato al punto 2

        KADAddress resKadAddress = new KADAddress(key.toString());

        //Find the resource's bucket
        //TODO gestire il caso del bucket inesistente
        int bucketIndex = mySelf.getNetworkAddress().firstDifferentBit(resKadAddress);


        //Get all users which are in the resource's bucket (possible candidates to handle it)
        ArrayList<SMSKADPeer> resCandidates = dict.getUsersInBucket(bucketIndex);

        //Compare users' addresses with resource's address to find the closest
        SMSKADPeer closestPeer = resCandidates.get(0);
        int minDistance = closestPeer.getNetworkAddress().firstDifferentBit(resKadAddress);
        for (SMSKADPeer possiblePeer : resCandidates) {
            if (possiblePeer.getNetworkAddress().firstDifferentBit(resKadAddress) < minDistance) {
                minDistance = possiblePeer.getNetworkAddress().firstDifferentBit(resKadAddress);
                closestPeer = possiblePeer;
            }
        }

        //FIXME: controllare se closestPeer è realmente il nodo più vicino (devo chiedergli se nei suoi bucket ha nodi più vicini alla risorsa)
        //       Per ora assegno la risorsa al nodo più vicino che conosco io

        //Assign the resource to closest peer
        SMSCommandMapper.sendRequest(Request.STORE, key.toString() + SPLIT_CHAR + value.toString(), closestPeer);

    }

    /**
     * Removes a key-value resource from the local dictionary: this is called if a STORE (key, NULL) message is received
     *
     * @param key The resource key
     */
    @Override
    public void removeResource(SerializableObject key) {
        setResource(key, null);
    }

    /**
     * Finds the peer of the given KADAddress object
     * If the given KADAddress doesn't exist then finds then peer of the closest (to the one given)
     *
     * @param kadAddress The KADAddress for which to find the peer
     * @return an {@link KADAddress} object
     */
    private KADPeer findAddressPeer(KADAddress kadAddress) {
        //TODO this only ask the closest node in our local dictionary to find closer node to the given KADAddress
        //TODO 1. Trovare il bucket in cui si trova il kadAddress o, se non esiste, quello più vicino ad esso
        //TODO 2. Confrontare gli indirizzi degli utenti di quel bucket con quello del kadAddress e prendo il più vicino
        //TODO 3. Estrapolare il peer dall'indirizzo trovato e ritornarlo
        return null;
    }


    /**
     * Republishes a key to be retained in the network
     *
     * @param key The key to be republished
     */
    public void republishKey(SerializableObject key) {
        //TODO Take a look to this method
        setResource(key, dict.getValue(new KADAddress(key.toString())));
    }

    /**
     * Method used to find a value of the given key
     *
     * @param resourceKey The resource key of which we want to find the value
     */
    @Override
    public void findValue(SerializableObject resourceKey, ReplyListener listener) {
        KADAddress key = new KADAddress(resourceKey.toString());
        SerializableObject value = dict.getValue(key);
        if (value != null) {
            listener.onValueReceived(value);
            return;
        }
        ArrayList<SMSKADPeer> peersThatMightHaveTheRes = dict.getUsersInBucket(key.firstDifferentBit(mySelf.networkAddress));
        if (peersThatMightHaveTheRes.size() == 0) {
            listener.onValueNotFound();
            return;
        }
        for (SMSKADPeer possiblePeer : peersThatMightHaveTheRes) {
            SMSCommandMapper.sendRequest(Request.FIND_VALUE, resourceKey.toString(), possiblePeer);
        }
        resourceListener = listener;
    }

    /**
     * This method is called when a join proposal is received.
     *
     * @param peer who invited you
     */
    protected void onJoinProposal(SMSPeer peer) {
        callbackListener.onJoinRequest(peer);
    }

    /**
     * Method called when a PING request has been received. Sends a {@link Reply#PING_ECHO) command back.
     *
     * @param peer
     */
    protected void onPingRequest(SMSPeer peer) {
        SMSCommandMapper.sendReply(Reply.PING_ECHO, "", peer);
    }

    protected void onFindNodeRequest(SMSPeer peer, String requestContent) {
        //TODO 1. Translate the peer into a SMSKADPeer
        //TODO 2. Vedere se conosciamo quel peer
        //TODO 2.1 Se si mandiamo una risposta di Reply#NODE_FOUND
        //TODO 2.2 Altrimenti mandiamo indietro il primo dei peer di quel bucket sempre con Reply#NODE_FOUND
    }

    protected void onFindValueRequest(SMSPeer peer, String requestContent) {

    }

    protected void onStoreRequest(SMSPeer peer, String requestContent) {
        //THIS COMMAND IS RECEIVED ONLY AFTER SEARCHING FOR THE CLOSEST NODE
        //TODO 1. Splittare la stringa per il SPLIT_CHAR
        //TODO 2. Chiamare split[0] è la chiave, key = KADAddress(split[0])
        //TODO 3. Chaiamare SerializableObject value = valueParser.parseString(split[1])
        //TODO 4. dict.setResource(key, value);
    }

    protected void onJoinReply(SMSPeer peer) {
        //TODO 1. Controlliamo che questo lo abbiamo invitato noi cercando se è presente nella lista degli invitati
        //TODO 2. Gli mandiamo tutte le persone che conosciamo noi tranne noi stessi, poi sarà lui a bucketarli
        //TODO    Il comando da usare non è ancora stato definito, sarebbe da definire un reuqest REGISTER_NODE
        //TODO 3. Aggiungiamo questo nuovo peer alla nostra lista dentro i bucket
    }

    protected void onPingReply(SMSPeer peer) {
        //Method called when someone you pinged gives you an answer
        //TODO ci dovrebbe essere un listener per quando si fanno i ping, se è != null va chiamato
    }

    protected void onNodeFoundReply(SMSPeer peer, String replyContent) {
        //IT'S NOT ALWAYS THE NODE WE WERE LOOKING FOR! MIGHT BE A CLOSER ONE

    }

    protected void onValueFoundReply(SMSPeer peer, String replyContent) {

    }

    enum Request {
        JOIN_PROPOSAL,
        PING,
        STORE,
        FIND_NODE,
        FIND_VALUE
    }

    enum Reply {
        JOIN_AGREED,
        PING_ECHO,
        NODE_FOUND,
        VALUE_FOUND
    }
}
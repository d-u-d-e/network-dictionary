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
 * @author Luca Crema
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
        //TODO gestire il caso del bucket inesistente -> ovvero andare a cercare nel bucket di indice precedente e via cosi
        int bucketIndex = mySelf.getNetworkAddress().firstDifferentBit(resKadAddress);


        //Get all users which are in the resource's bucket (possible candidates to handle it)
        ArrayList<SMSKADPeer> resCandidates = dict.getUsersInBucket(bucketIndex);

        //Compare users' addresses with resource's address to find the closest

        //TODO here we should use findAddressPeer

        SMSKADPeer closestPeer = resCandidates.get(0);
        int closestBucketIndex = closestPeer.getNetworkAddress().firstDifferentBit(resKadAddress);
        for (SMSKADPeer possiblePeer : resCandidates) {
            int index = possiblePeer.getNetworkAddress().firstDifferentBit(resKadAddress);
            if (index > closestBucketIndex) {
                closestBucketIndex = index;
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
        //TODO this call crashes because value is referenced (value.toString()) in setResource(key , value)
        //TODO we should break setResource in two parts: 1) we first find the closest node to a key using findAddressPeer, 2) and then we send the store request
        //TODO so we can then use 1) to find the owner of key, and then simply call something like sendRequest(STORE, key.toString()  + "_" + "NULL", closestPeer)
        //setResource(key, null);
    }

    /**
     * Finds the peer of the given KADAddress object
     * If the given KADAddress doesn't exist then finds then peer of the closest (to the one given)
     *
     * @param kadAddress The KADAddress for which to find the peer
     * @return an {@link KADAddress} object
     */
    private KADPeer findAddressPeer(KADAddress kadAddress) {
        //TODO this only asks the closest nodes in our local dictionary to find closer nodes to the given KADAddress
        //TODO 1. Trovare il bucket in cui si trova il kadAddress o, se non esiste, quello più vicino ad esso
        //TODO 2. Confrontare gli indirizzi degli utenti di quel bucket con quello del kadAddress e prendo il più vicino
        //TODO 3. Estrapolare il peer dall'indirizzo trovato e inviargli una richiesta di find dell'indirizzo.
        //TODO 4. Procedere in modo ricorsivo sino a quando ricevo il numero di telefono del peer più vicino in assoluto a kadAddress


        //Find the user's bucket
        //TODO gestire il caso del bucket inesistente -> ovvero andare a cercare nel bucket di indice precedente e via cosi
        int userBucket = mySelf.getNetworkAddress().firstDifferentBit(kadAddress);

        //Create an array with all the bucket users
        ArrayList<SMSKADPeer> bucketUsers = dict.getUsersInBucket(userBucket);

        //Control if in the bucket there is the given address, if yes return it
        for (SMSKADPeer possibleUser : bucketUsers) {
            if (possibleUser.getNetworkAddress().firstDifferentBit(kadAddress) == -1)
                return possibleUser;
        }

        //If the the user associated with the given KADAddress doesn't exist in the bucket then return the KADAddress peer of the closest user address
        SMSKADPeer closestPeer = bucketUsers.get(0);
        int closestBucketIndex = closestPeer.getNetworkAddress().firstDifferentBit(kadAddress);
        for (SMSKADPeer possiblePeer : bucketUsers) {
            int index = possiblePeer.getNetworkAddress().firstDifferentBit(kadAddress);
            if (index > closestBucketIndex) {
                closestBucketIndex = index;
                closestPeer = possiblePeer;
            }
        }

        //TODO SMSCommandMapper.sendRequest(Request.FIND_NODE, ???????????);

        return closestPeer;
    }


    /**
     * Republishes a key to be retained in the network
     *
     * @param key The key to be republished
     */
    public void republishKey(SerializableObject key) {
        //TODO Take a look at this method
        //setResource(key, dict.getValue(new KADAddress(key.toString())));
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

        //TODO this is similar to findAddressPeer, except that when the value is found it is immediately returned

        ArrayList<SMSKADPeer> peersThatMightHaveTheRes = dict.getUsersInBucket(key.firstDifferentBit(mySelf.networkAddress));
        if (peersThatMightHaveTheRes.size() == 0) {
            listener.onValueNotFound();
            return;
        }
        resourceListener = listener;
        for (SMSKADPeer possiblePeer : peersThatMightHaveTheRes) {
            SMSCommandMapper.sendRequest(Request.FIND_VALUE, resourceKey.toString(), possiblePeer);
        }
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
     * @param peer who requested a ping
     */
    protected void onPingRequest(SMSPeer peer) {
        //TODO should add a randomID to mach ping requests: take a look at sms formats requests
        SMSCommandMapper.sendReply(Reply.PING_ECHO, "", peer);
    }

    /**
     * Method called when a FIND_NODE request is received. Sends a {@link Reply#NODE_FOUND} command back.
     *
     * @param peer           who requested the node search
     * @param requestContent information about the node to find, must be parsed first
     */
    protected void onFindNodeRequest(SMSPeer peer, String requestContent) {
        //TODO 1. Translate the peer into a SMSKADPeer
        //TODO 2. Vedere se conosciamo quel peer
        //TODO 2.1 Se si mandiamo una risposta di Reply#NODE_FOUND
        //TODO 2.2 Altrimenti mandiamo indietro il primo dei peer di quel bucket sempre con Reply#NODE_FOUND

        KADAddress peerToSearch = new KADAddress(requestContent);

        int bucketIndex = mySelf.getNetworkAddress().firstDifferentBit(peerToSearch);
        ArrayList<SMSKADPeer> knownPeers = dict.getUsersInBucket(bucketIndex);

        for (SMSKADPeer possible : knownPeers) {
            if (((possible.getNetworkAddress()).firstDifferentBit(peerToSearch)) == -1) {
                SMSCommandMapper.sendReply(Reply.NODE_FOUND, peer);
                return;
            }
        }

        //TODO: CONTROLLARE IL CONTENT DELLA REPLY
        SMSCommandMapper.sendReply(Reply.NODE_FOUND, knownPeers.get(0).toString(), peer);
    }

    /**
     * Method called when a FIND_VALUE request is received. Sends a {@link Reply#VALUE_FOUND} command back.
     *
     * @param peer           who requested the value
     * @param requestContent information about the value to find, must be parsed first
     */
    protected void onFindValueRequest(SMSPeer peer, String requestContent) {

    }

    /**
     * Method called when a STORE request is received.
     *
     * @param peer           who requested you to store data
     * @param requestContent information about the key/value to store, must be parsed.
     */
    protected void onStoreRequest(SMSPeer peer, String requestContent) {
        //THIS COMMAND IS RECEIVED ONLY AFTER SEARCHING FOR THE CLOSEST NODE
        //THIS AUTOMATICALLY MEANS I AM THE CLOSEST PEER TO the key specified
        //TODO 1. Splittare la stringa per il SPLIT_CHAR
        //TODO 2. split[0] è la chiave, key = KADAddress(split[0])
        //TODO 3. SerializableObject value = valueParser.parseString(split[1])
        //TODO 4. dict.setResource(key, value);
    }

    /**
     * Method called when a JOIN_AGREED reply is received.
     *
     * @param peer user that has just joined.
     */
    protected void onJoinReply(SMSPeer peer) {
        //TODO 1. Controlliamo che questo lo abbiamo invitato noi cercando se è presente nella lista degli invitati
        //TODO 2. Gli mandiamo tutte le persone che conosciamo noi tranne noi stessi, poi sarà lui a bucketarli
        //TODO    Il comando da usare non è ancora stato definito, sarebbe da definire un request REGISTER_NODE
        //TODO 3. Aggiungiamo questo nuovo peer alla nostra lista dentro i bucket
    }

    /**
     * Method called when a PING_ECHO reply is received. We are sure this node is alive.
     *
     * @param peer user that replied to the ping.
     */
    protected void onPingReply(SMSPeer peer) {
        //Method called when someone you pinged gives you an answer
        //TODO ci dovrebbe essere un listener per quando si fanno i ping, se è != null va chiamato
    }

    /**
     * Method called when a NODE_FOUND reply is received.
     *
     * @param peer         user that replied to the FIND_NODE.
     * @param replyContent information about the node, must be parsed
     */
    protected void onNodeFoundReply(SMSPeer peer, String replyContent) {
        //IT'S NOT ALWAYS THE NODE WE WERE LOOKING FOR! MIGHT BE A CLOSER ONE

    }

    /**
     * Method called when a VALUE_FOUND reply is received.
     *
     * @param peer         user that replied to FIND_VALUE
     * @param replyContent information about the value, must be parsed
     */
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
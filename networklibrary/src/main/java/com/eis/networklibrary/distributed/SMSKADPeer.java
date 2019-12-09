package com.eis.networklibrary.distributed;

import com.eis.smslibrary.SMSPeer;

public class SMSKADPeer extends KADPeer
{
    SMSPeer smsPeer;
    public SMSKADPeer(SMSPeer peer){
        super(peer);
        this.smsPeer = peer;
    }
}

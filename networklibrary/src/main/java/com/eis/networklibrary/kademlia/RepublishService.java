package com.eis.networklibrary.kademlia;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class RepublishService extends TimerTask {
    @Override
    public void run() {
        SMSNetworkManager manager = SMSNetworkManager.getInstance();
        manager.republishKeys();
    }

    public static void startTask(Date beginAt){
        RepublishService service = new RepublishService();
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(service, beginAt, SMSNetworkManager.KADEMLIA_REPUBLISH_PERIOD);
    }
}

package com.eis.networklibrary.kademlia;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Republish service: every <i>KADEMLIA_REPUBLISH_PERIOD_MILLIS</i> ms, each key is republished.
 * @author Marco Mariotto
 */
public class RepublishService extends TimerTask {
    @Override
    public void run() {
        SMSNetworkManager manager = SMSNetworkManager.getInstance();
        manager.republishKeys();
    }

    /**
     * @param beginAt date that specifies when to start this task
     */
    public static void startTask(Date beginAt){
        RepublishService service = new RepublishService();
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(service, beginAt, SMSNetworkManager.KADEMLIA_REPUBLISH_PERIOD_MILLIS);
    }
}

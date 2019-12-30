package com.eis.networklibrary.kademlia;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Republish service: every <i>KADEMLIA_REPUBLISH_PERIOD_MILLIS</i> ms, each key is republished.
 * @author Marco Mariotto
 */
public class RepublishService extends TimerTask {
    static final String DATE_INVALID = "Must pass a valid date: this task can't be started before the current time";
    @Override
    public void run() {
        SMSNetworkManager manager = SMSNetworkManager.getInstance();
        manager.republishKeys();
    }

    /**
     * Creates a RepublishService and schedules it at the specified date {@code beginAt}
     * @param beginAt date that specifies when to start this task
     */
    static void startTask(Date beginAt){
        if(beginAt.getTime() < System.currentTimeMillis())
            throw new IllegalArgumentException(DATE_INVALID);
        RepublishService service = new RepublishService();
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(service, beginAt, SMSNetworkManager.KADEMLIA_REPUBLISH_PERIOD_MILLIS);
    }
}

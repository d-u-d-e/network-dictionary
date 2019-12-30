package com.eis.networklibrary.kademlia;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static com.eis.networklibrary.kademlia.SMSDistributedNetworkDictionary.NO_BUCKETS;
import static com.eis.networklibrary.kademlia.SMSNetworkManager.KADEMLIA_REFRESH_PERIOD_MILLIS;

/**
 * Refresh service: every <i>KADEMLIA_REFRESH_PERIOD_MILLIS/2</i> ms, it refreshes any bucket to which the node has not
 * performed a node lookup in the past <i>KADEMLIA_REFRESH_PERIOD_MILLIS/2</i> ms.
 * This guarantees that the last refresh is at most <i>KADEMLIA_REFRESH_PERIOD_MILLIS</i> ms old.
 * @author Marco Mariotto
 */
public class RefreshService extends TimerTask {
    static final String DATE_INVALID = "Must pass a valid date: this task can't be started before the current time";

    @Override
    public void run() {
        SMSNetworkManager manager = SMSNetworkManager.getInstance();
        for(int i = 0; i < NO_BUCKETS; i++)
            synchronized (manager.lastRefresh){
                if(System.currentTimeMillis() - manager.lastRefresh[i] > KADEMLIA_REFRESH_PERIOD_MILLIS / 2)
                    manager.refreshBucket(i);
            }
    }

    /**
     * Creates a RefreshService and schedules it at the specified date {@code beginAt}
     * @param beginAt date that specifies when to start this task
     */
    static void startTask(Date beginAt){
        if(beginAt.getTime() < System.currentTimeMillis())
            throw new IllegalArgumentException(DATE_INVALID);
        RefreshService service = new RefreshService();
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(service, beginAt, KADEMLIA_REFRESH_PERIOD_MILLIS / 2);
    }
}

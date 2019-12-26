package com.eis.networklibrary.kademlia;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static com.eis.networklibrary.kademlia.SMSDistributedNetworkDictionary.NO_BUCKETS;
import static com.eis.networklibrary.kademlia.SMSNetworkManager.KADEMLIA_REFRESH_PERIOD_MILLIS;

/**
 * Refresh service: every KADEMLIA_REFRESH_PERIOD_MILLIS/2 ms, it refreshes any bucket to which the node has not
 * performed a node lookup in the past KADEMLIA_REFRESH_PERIOD_MILLIS/2 ms.
 * This guarantees that the last refresh is at most KADEMLIA_REFRESH_PERIOD_MILLIS ms old.
 * @author Marco Mariotto
 */
public class RefreshService extends TimerTask {
    @Override
    public void run() {
        SMSNetworkManager manager = SMSNetworkManager.getInstance();
        for(int i = 0; i < NO_BUCKETS; i++)
            synchronized (manager.lastRefresh){
                if(System.currentTimeMillis() - manager.lastRefresh[i] > KADEMLIA_REFRESH_PERIOD_MILLIS / 2)
                    manager.refreshBucket(i);
            }
    }

    public static void startTask(Date beginAt){
        RefreshService service = new RefreshService();
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(service, beginAt, KADEMLIA_REFRESH_PERIOD_MILLIS / 2);
    }
}

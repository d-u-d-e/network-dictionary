package com.eis.networklibrary.kademlia;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static com.eis.networklibrary.kademlia.SMSDistributedNetworkDictionary.NO_BUCKETS;
import static com.eis.networklibrary.kademlia.SMSNetworkManager.KADEMLIA_REFRESH_PERIOD;

public class RefreshService extends TimerTask {
    @Override
    public void run() {
        SMSNetworkManager manager = SMSNetworkManager.getInstance();
        for(int i = 0; i < NO_BUCKETS; i++)
            synchronized (manager.lastRefresh){
                if(System.currentTimeMillis() - manager.lastRefresh[i] > KADEMLIA_REFRESH_PERIOD / 2)
                    manager.refreshBucket(i);
            }
    }

    public static void startTask(Date beginAt){
        RefreshService service = new RefreshService();
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(service, beginAt, KADEMLIA_REFRESH_PERIOD / 2);
    }
}

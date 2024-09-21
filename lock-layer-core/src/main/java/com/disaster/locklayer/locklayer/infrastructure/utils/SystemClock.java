package com.disaster.locklayer.locklayer.infrastructure.utils;

import java.sql.Timestamp;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>
 * Performance optimization of System.current Time Millis() in high concurrency scenarios and time stamps in LRU list in redis are implemented in this way (very efficient and useful).
 * </p>
 * <p>
 * The System.current Time Millis() call takes much longer than a new normal object<br>
 * System.current Time Millis() is slow because it has dealt with the system once<br>
 * The background regularly updates the clock, and when the JVM exits, the thread is automatically reclaimed<br>
 * 10 Hundred million：43410,206,210.72815533980582%<br>
 * 1 Hundred million：4699,29,162.0344827586207%<br>
 * 1000 ten thousand：480,12,40.0%<br>
 * 100 ten thousand：50,10,5.0%<br>
 * </p>
 */
public class SystemClock {

    private final long period;
    private final AtomicLong now;

    private SystemClock(long period) {
        this.period = period;
        this.now = new AtomicLong(System.currentTimeMillis());
        scheduleClockUpdating();
    }

    private static SystemClock instance() {
        return InstanceHolder.INSTANCE;
    }

    public static long now() {
        return instance().currentTimeMillis();
    }

    public static String nowDate() {
        return new Timestamp(instance().currentTimeMillis()).toString();
    }

    private void scheduleClockUpdating() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "System Clock");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleAtFixedRate(() ->
                now.set(System.currentTimeMillis()), period, period, TimeUnit.MILLISECONDS);
    }

    private long currentTimeMillis() {
        return now.get();
    }

    private static class InstanceHolder {

        public static final SystemClock INSTANCE = new SystemClock(1);
    }

}

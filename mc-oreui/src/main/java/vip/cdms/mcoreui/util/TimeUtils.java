package vip.cdms.mcoreui.util;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 仿js时间小工具
 * @author Cdm2883
 */
public class TimeUtils {
    public static final ArrayList<Timer> timeoutQueue = new ArrayList<>();
    public static final ArrayList<Timer> intervalQueue = new ArrayList<>();

    public static int setTimeout(Runnable runnable, int delay) {
        int id = timeoutQueue.size();
        Timer timer = new Timer();
        timeoutQueue.add(id, timer);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (id >= timeoutQueue.size() || timeoutQueue.get(id) == null) return;
                runnable.run();
                timeoutQueue.set(id, null);
                cancel();
            }
        }, delay);
        return id;
    }
    public static void clearTimeout(int id) {
        Timer timer = timeoutQueue.get(id);
        if (timer == null) return;
        timer.cancel();
        timeoutQueue.set(id, null);
    }

    public static int setInterval(Runnable runnable, int period) {
        int id = intervalQueue.size();
        Timer timer = new Timer();
        intervalQueue.add(id, timer);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (id >= intervalQueue.size() || intervalQueue.get(id) == null) return;
                runnable.run();
            }
        }, 0, period);
        return id;
    }
    public static void clearInterval(int id) {
        Timer timer = intervalQueue.get(id);
        if (timer == null) return;
        timer.cancel();
        intervalQueue.set(id, null);
    }

    public static class Task {
        private Runnable runnable;
        public Task setRunnable(Runnable runnable) {
            this.runnable = runnable;
            return this;
        }
        public void run() {
            runnable.run();
        }
    }
    public static class ThrottleTask extends Task {
        private final int wait;
        private long lastRun = -1;
        public ThrottleTask(int wait) {
            this.wait = wait;
        }
        @Override
        public void run() {
            long now = System.currentTimeMillis();
            if (now - lastRun < wait) return;
            lastRun = now;
            super.run();
        }
    }
}

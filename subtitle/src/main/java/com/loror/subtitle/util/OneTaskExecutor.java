package com.loror.subtitle.util;

import java.util.concurrent.Executor;

public class OneTaskExecutor implements Executor {

    private Thread thread;
    private Runnable task;
    private long lastTime;

    @Override
    public void execute(Runnable command) {
        synchronized (OneTaskExecutor.class) {
            if (thread == null) {
                thread = createThread();
                thread.start();
            }
            this.task = command;
        }
    }

    private Thread createThread() {
        lastTime = System.currentTimeMillis();
        return new Thread() {
            @Override
            public void run() {
                for (; ; ) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                    Runnable run;
                    synchronized (OneTaskExecutor.class) {
                        run = task;
                        task = null;
                    }
                    if (run != null) {
                        run.run();
                        lastTime = System.currentTimeMillis();
                    } else {
                        long now = System.currentTimeMillis();
                        //空闲10s回收
                        if (now - lastTime > 10000) {
                            synchronized (OneTaskExecutor.class) {
                                if (task != null) {
                                    continue;
                                }
                                break;
                            }
                        }
                    }
                }
                synchronized (OneTaskExecutor.class) {
                    thread = null;
                }
            }
        };
    }

    public void clear() {
        synchronized (OneTaskExecutor.class) {
            if (thread != null) {
                thread.interrupt();
            }
            this.task = null;
        }
    }
}

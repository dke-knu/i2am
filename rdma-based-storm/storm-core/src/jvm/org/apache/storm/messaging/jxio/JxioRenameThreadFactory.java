package org.apache.storm.messaging.jxio;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by seokwoo on 2017-05-12.
 */
public class JxioRenameThreadFactory implements ThreadFactory {

    final ThreadGroup group;
    final AtomicInteger index = new AtomicInteger(1);
    final String name;
    static final JxioUncaughtExceptionHandler uncaughtExceptionHandler = new JxioUncaughtExceptionHandler();

    public JxioRenameThreadFactory(String name) {
        SecurityManager s = System.getSecurityManager();
        group = (s != null)? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        this.name = name;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r, name + "-" + index.getAndIncrement(), 0);
        if(t.isDaemon()) {
            t.setDaemon(false);
        }
        if(t.getPriority() != Thread.NORM_PRIORITY) {
            t.setPriority(Thread.NORM_PRIORITY);
        }
        t.setUncaughtExceptionHandler(uncaughtExceptionHandler);
        return t;
    }
}

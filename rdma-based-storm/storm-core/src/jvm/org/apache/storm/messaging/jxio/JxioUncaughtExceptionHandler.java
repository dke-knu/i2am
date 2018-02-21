package org.apache.storm.messaging.jxio;

import org.apache.storm.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by seokwoo on 2017-05-12.
 */
public class JxioUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(JxioUncaughtExceptionHandler.class);
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        try {
            Utils.handleUncaughtException(e);
        } catch (Error error) {
            LOG.error("Received error in JXIO thread.. terminating server...");
            Runtime.getRuntime().exit(1);
        }
    }
}

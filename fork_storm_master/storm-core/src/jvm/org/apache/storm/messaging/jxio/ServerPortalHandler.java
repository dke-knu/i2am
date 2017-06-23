package org.apache.storm.messaging.jxio;

import org.accelio.jxio.*;
import org.apache.storm.Config;
import org.apache.storm.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by admin on 17. 6. 8.
 */
public class ServerPortalHandler extends Thread implements Comparable<ServerPortalHandler>, WorkerCache.Worker {
    private final static Logger LOG = LoggerFactory.getLogger(ServerPortalHandler.class.getCanonicalName());

    private final ServerPortal sp;
    private final EventQueueHandler eqh;
    private final MsgPool msgPool;
    public final int portal_index;
    private AtomicInteger num_of_sessions;
    private ServerSessionHandler handler;

    public ServerPortalHandler(int index, URI uri, ServerPortal.Callbacks c) {
        portal_index = index;
        eqh = new EventQueueHandler(new EqhCallbacks(200, 262160, 200));
        msgPool = new MsgPool(500, 262160, 200);
        eqh.bindMsgPool(msgPool);
        sp = new ServerPortal(eqh, uri, c);
        num_of_sessions = new AtomicInteger(0);
    }

    public void run() {
        LOG.info("Server worker number " + portal_index + " is up and waiting for requests");
        eqh.run();
    }

    public ServerPortal getPortal() {
        return sp;
    }

    public EventQueueHandler getEqh() {
        return eqh;
    }

    @Override
    public int compareTo(ServerPortalHandler s) {
        if (this.num_of_sessions.get() <= s.num_of_sessions.get()) {
            return -1;
        } else {
            return 1;
        }
    }

    @Override
    public boolean isFree() {
        if (handler == null) {
            return true;
        } else {
            return (handler.getSession() == null);
        }
    }

    public ServerSessionHandler getHandler() {
        return handler;
    }

    public void setSessionHandler(ServerSessionHandler handler) {
        this.handler = handler;
    }

    public void disconnect() {
        handler.getSession().close();
        eqh.breakEventLoop();
    }

    class EqhCallbacks implements EventQueueHandler.Callbacks {
        private final int numMsgs;
        private final int inMsgSize;
        private final int outMsgSize;

        public EqhCallbacks(int msgs, int in, int out) {
            numMsgs = msgs;
            inMsgSize = in;
            outMsgSize = out;
        }

        public MsgPool getAdditionalMsgPool(int in, int out) {
            MsgPool mp = new MsgPool(numMsgs, inMsgSize, outMsgSize);
            LOG.warn("new MsgPool: " + mp);
            return mp;
        }
    }
}

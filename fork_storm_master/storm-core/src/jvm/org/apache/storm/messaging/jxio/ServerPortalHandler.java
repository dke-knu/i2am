package org.apache.storm.messaging.jxio;

import org.accelio.jxio.EventQueueHandler;
import org.accelio.jxio.MsgPool;
import org.accelio.jxio.ServerPortal;
import org.apache.storm.Config;
import org.apache.storm.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by admin on 17. 6. 8.
 */
public class ServerPortalHandler extends Thread {
    private final static Logger LOG = LoggerFactory.getLogger(ServerPortalHandler.class.getCanonicalName());

    private final ServerPortal sp;
    private final EventQueueHandler eqh;
    private final MsgPool msgPool;
    private final int portal_index;
    private AtomicInteger num_of_sessions;

    public ServerPortalHandler(int index, URI uri, ServerPortal.Callbacks c) {
        portal_index = index;
        eqh = new EventQueueHandler(new EqhCallbacks(262160, 200, 200));

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

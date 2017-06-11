package org.apache.storm.messaging.jxio;

import org.accelio.jxio.EventName;
import org.accelio.jxio.EventReason;
import org.accelio.jxio.Msg;
import org.accelio.jxio.ServerSession;
import org.accelio.jxio.exceptions.JxioGeneralException;
import org.accelio.jxio.exceptions.JxioSessionClosedException;
import org.apache.storm.messaging.TaskMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by admin on 17. 6. 9.
 */
public class ServerSessionHandler {
    private final static Logger LOG = LoggerFactory.getLogger(ServerSessionHandler.class.getCanonicalName());

    private final ServerSession session;
    private final ServerPortalHandler sph;
    public ServerSessionHandler(ServerSession.SessionKey sesKey, ServerPortalHandler sph, Server server) {
        session = new ServerSession(sesKey, new ServerSessionCallbacks(server));
        this.sph = sph;
    }

    public ServerSession getSession() {
        return session;
    }

    public class ServerSessionCallbacks implements ServerSession.Callbacks {
        private Server server;
        private List<TaskMessage> messages = new ArrayList<TaskMessage>();
        private AtomicInteger failure_count;

        public ServerSessionCallbacks(Server server) {
            this.server = server;
            failure_count = new AtomicInteger(0);
        }

        private Object decoder(ByteBuffer buf) {
            long available = buf.remaining();
            if (available < 2) {
                //need more data
                return null;
            }
            List<Object> ret = new ArrayList<>();

            //Use while loop, try to decode as more messages as possible in single call
            while (available >= 2) {

                // Mark the current buffer position before reading task/len field
                // because the whole frame might not be in the buffer yet.
                // We will reset the buffer position to the marked position if
                // there's not enough bytes in the buffer.
                buf.mark();

                short code = buf.getShort();
                available -= 2;

                //case 1: Control message
                //who send controlmessage?
                ControlMessage ctrl_msg = ControlMessage.mkMessage(code);
                if (ctrl_msg != null) {
                    if (ctrl_msg == ControlMessage.EOB_MESSAGE) {
                        continue;
                    } else {
                        return ctrl_msg;
                    }
                }
                //case 2: SaslTokenMeesageRequest
                //skip

                //case 3: TaskMessage
                //Make sure that we have received at least an integer (length)
                if (available < 4) {
                    //need more data
                    buf.reset();
                    break;
                }
                //Read the length field.
                int length = buf.getInt();
                available -= 4;

                if (length <= 0) {
                    ret.add(new TaskMessage(code, null));
                    break;
                }

                //Make sure if there's enough bytes in the buffer.
                if (available < length) {
                    //The whole bytes were not received yet - return null.
                    buf.reset();
                    break;
                }
                available -= length;

                //There's enough bytes in the buffer. Read it.
                byte[] payload = new byte[length];
                buf.get(payload);

                //Successfully decoded a frame.
                //Return a TaskMessage object
                ret.add(new TaskMessage(code, payload));
            }

            if (ret.size() == 0) {
                return null;
            } else {
                return ret;
            }
        }

        @Override
        public void onRequest(Msg msg) {

            if (!msg.getIn().hasRemaining()) {
                LOG.error("[Server-onRequest] msg.getIn is null, no messages in msg");
                return;
            }

            Object obj = decoder(msg.getIn());
            if (obj instanceof ControlMessage) {
                LOG.info("[Server] onRequest2");
                ControlMessage ctrl_msg = (ControlMessage) obj;
                LOG.info("[Server] onRequest3");
                if (ctrl_msg == ControlMessage.LOADMETRICS_REQUEST) {
                    LOG.info("[Server] onRequest4");
                    try {
                        LOG.info("[Server] onRequest5");
                        TaskMessage tm = new TaskMessage(-1, server._ser.serialize(Arrays.asList((Object) server.taskToLoad)));
                        LOG.info("[Server] onRequest6");
                        msg.getOut().put(tm.serialize());
                        LOG.info("[Server] onRequest7");
                        session.sendResponse(msg);
                        LOG.info("[Server] onRequest8");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                msg.getIn().rewind();
                ByteBuffer bb = msg.getIn();
                byte[] ipByte = new byte[13];
                bb.get(ipByte);
                String ipStr = new String(ipByte);
                LOG.info("[Server] normal messages from {}", ipStr);

                //single TaskMessage
            /*int taskId = bb.getShort();
            byte[] tempByte = new byte[bb.limit()-15];
            bb.get(tempByte);
            TaskMessage tm = new TaskMessage(taskId, tempByte);
            messages.add(tm);*/

                //batch TaskMessage
                List<TaskMessage> messages = (ArrayList<TaskMessage>) decoder(msg.getIn());

                try {
                    server.received(messages, ipStr);
                } catch (InterruptedException e) {
                    LOG.info("failed to enqueue a request message", e);
                    failure_count.incrementAndGet();
                    e.printStackTrace();
                }
                char ch = 's';
                msg.getOut().put((byte) ch);
                try {
                    session.sendResponse(msg);
                } catch (JxioGeneralException e) {
                    e.printStackTrace();
                } catch (JxioSessionClosedException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onSessionEvent(EventName eventName, EventReason eventReason) {
            String str = "[ServerSession][EVENT] Got event " + eventName + " because of " + eventReason;
            if (eventName == EventName.SESSION_CLOSED) { // normal exit
                LOG.info(str);
                sph.sessionClosed();
            } else {
                LOG.error(str);
            }
        }

        @Override
        public boolean onMsgError(Msg msg, EventReason eventReason) {
            if(ServerSessionHandler.this.session.getIsClosing()) {
                LOG.info("On Message Error while closing. Reason is = " + eventReason);
            } else {
                LOG.error("On Message Error. Reason is = " + eventReason);
            }
            return true;
        }
    }
}

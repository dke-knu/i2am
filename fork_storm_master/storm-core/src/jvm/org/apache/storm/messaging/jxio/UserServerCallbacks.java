/*
 ** Copyright (C) 2013 Mellanox Technologies
 **
 ** Licensed under the Apache License, Version 2.0 (the "License");
 ** you may not use this file except in compliance with the License.
 ** You may obtain a copy of the License at:
 **
 ** http://www.apache.org/licenses/LICENSE-2.0
 **
 ** Unless required by applicable law or agreed to in writing, software
 ** distributed under the License is distributed on an "AS IS" BASIS,
 ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 ** either express or implied. See the License for the specific language
 ** governing permissions and  limitations under the License.
 **
 */
package org.apache.storm.messaging.jxio;

import org.apache.storm.messaging.TaskMessage;
import org.accelio.jxio.jxioConnection.JxioConnectionServer;
import org.apache.storm.serialization.KryoValuesSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class UserServerCallbacks implements JxioConnectionServer.Callbacks {

    private static final Logger LOG = LoggerFactory.getLogger(UserServerCallbacks.class.getCanonicalName());
    private Server server;
    private AtomicInteger failure_count;
    private KryoValuesSerializer _ser;
    private Map<Integer, Double> taskToLoad;
    private int msgpool_size;

    public void setTaskToLoad(Map<Integer, Double> taskToLoad) {
        this.taskToLoad = taskToLoad;
    }

    public UserServerCallbacks(Server server, KryoValuesSerializer ser, int msgpool_size) {
        this.server = server;
        this._ser = ser;
        this.msgpool_size = msgpool_size;
        failure_count = new AtomicInteger(0);
    }

    public void newSessionOS(URI uri, OutputStream output) {
        // Only sendMetrics method send to clients
        LOG.info("newSessionOS invoked");
        try {
            TaskMessage tm = new TaskMessage(-1, _ser.serialize(Arrays.asList((Object) taskToLoad)));
            output.write(tm.serialize().get());
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    //uri of EventNewSession.getUri()
    @Override
    public void newSessionIS(URI uri, InputStream input) {
        byte[] temp = new byte[msgpool_size];
        long bytes = getBytes(uri);
        List<TaskMessage> messages = new ArrayList<>();
        LOG.info(Thread.currentThread().toString() + " going to read " + bytes + " bytes");

        try {
            int num;
            int read=0;
            while ((num = input.read(temp)) != -1) {
                read+=num;
                ByteBuffer bb = ByteBuffer.wrap(temp);
                TaskMessage taskMessage = new TaskMessage(0, null);
                taskMessage.deserialize(bb);
                messages.add(taskMessage);
            LOG.info("newSessionIS invoked, message: " + new String(taskMessage.message()) + "remote IP: " + uri.getHost());

            }
            if(read != bytes) LOG.error("Number of bytes read " + read + " is different from number of bytes requested " + bytes);

            server.received(messages, uri.getHost());

        } catch (IOException e) {
            LOG.error(Thread.currentThread().toString() + " Error reading data, " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            LOG.info("failed to enqueue a request message", e);
            failure_count.incrementAndGet();
            e.printStackTrace();
        }

    }

    public long getBytes(URI uri) {
        String query = uri.getQuery();
        return Long.parseLong(query.split("size=")[1].split("&")[0]);
    }
}
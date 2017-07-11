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

import org.accelio.jxio.jxioConnection.*;
import org.apache.storm.messaging.TaskMessage;
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

    public UserServerCallbacks(Server server, KryoValuesSerializer ser) {
        this.server = server;
        this._ser = ser;
        failure_count = new AtomicInteger(0);
    }

    public void newSessionOS(URI uri, OutputStream output) {
        // Only sendMetrics method send to clients
        LOG.info("newSessionOS invoked");
        byte[] temp = new byte[100];
        try {
            TaskMessage tm = new TaskMessage(-1, _ser.serialize(Arrays.asList((Object) taskToLoad)));
            ByteBuffer bb = tm.serialize();
            bb.get(temp);

            long bytes = bb.limit();
            long sent = 0;
            int num = 0;

            while (sent < bytes) {
                num = (int) Math.min(bytes - sent, temp.length);
                output.write(temp, 0, num);
                sent += num;
            }
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    //uri of EventNewSession.getUri()
    @Override
    public void newSessionIS(URI uri, InputStream input) {
        byte[] temp = new byte[Constants.MSGPOOL_BUF_SIZE];


    }

    public long getBytes(URI uri) {
        String query = uri.getQuery();
        return Long.parseLong(query.split("size=")[1].split("&")[0]);
    }
}
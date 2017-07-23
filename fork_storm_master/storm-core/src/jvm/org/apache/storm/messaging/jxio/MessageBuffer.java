/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.storm.messaging.jxio;

import org.apache.storm.messaging.TaskMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulates the state used for batching up messages.
 */
public class MessageBuffer {
    private final int mesageBatchSize;
    private MessageBatch currentBatch;
    private final byte[] fromIp;
    private int availableSize;

    public MessageBuffer(int mesageBatchSize, byte[] fromIp) {
        this.fromIp = fromIp;
        this.mesageBatchSize = mesageBatchSize;
        this.currentBatch = new MessageBatch(mesageBatchSize, fromIp);
        availableSize = mesageBatchSize;
    }

    public void add(TaskMessage msg) {
        availableSize = currentBatch.add(msg);
    }

    /*public MessageBatch add(TaskMessage msg) {
        currentBatch.add(msg);
        if(currentBatch.isFull()){
            MessageBatch ret = currentBatch;
            currentBatch = new MessageBatch(mesageBatchSize, fromIp);
            return ret;
        } else {
            return null;
        }
    }*/

    public boolean isAvailable(int size) {
        //size + int + short (payload_len & code)
        return (size + 6) <= availableSize;
    }

    public MessageBatch getCurrentBatch() {
        MessageBatch ret = currentBatch;
        currentBatch = new MessageBatch(mesageBatchSize, fromIp);
        return ret;
    }

    public boolean isEmpty() {
        return currentBatch.isEmpty();
    }

    public MessageBatch drain() {
        if (!currentBatch.isEmpty()) {
            MessageBatch ret = currentBatch;
            currentBatch = new MessageBatch(mesageBatchSize, fromIp);
            return ret;
        } else {
            return null;
        }
    }
}

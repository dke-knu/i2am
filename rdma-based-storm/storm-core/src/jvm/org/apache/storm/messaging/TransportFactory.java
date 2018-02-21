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
package org.apache.storm.messaging;

import org.apache.storm.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;

public class TransportFactory {
    public static final Logger LOG = LoggerFactory.getLogger(TransportFactory.class);

    static private final String JXIO_CONTEXT = "org.apache.storm.messaging.jxio.Context";
    static private final String NETTY_CONTEXT = "org.apache.storm.messaging.netty.Context";

    public static IContext makeContext(Map storm_conf) {

        //get factory class name
        String transport_plugin_klassName = null;
//                = (String)storm_conf.get(Config.STORM_MESSAGING_TRANSPORT);

        String transport_mode = (String) storm_conf.get(Config.STORM_MESSAGING_TRANSFER_MODE);

        LOG.info("#####" + transport_mode);

        //choose which plugin used
        if ("RDMA".equals(transport_mode))
            transport_plugin_klassName = JXIO_CONTEXT;
        else if ("Netty".equals(transport_mode))
            transport_plugin_klassName = NETTY_CONTEXT;
        else
            throw new RuntimeException("can't read context (" + transport_mode + ") ");

        LOG.info("Storm peer transport plugin:" + transport_plugin_klassName);
        IContext transport;
        try {
            //create a factory class
            Class klass = Class.forName(transport_plugin_klassName);
            //obtain a context object
            Object obj = klass.newInstance();
            if (obj instanceof IContext) {
                //case 1: plugin is a IContext class
                transport = (IContext) obj;
                //initialize with storm configuration
                transport.prepare(storm_conf);
            } else {
                //case 2: Non-IContext plugin must have a makeContext(storm_conf) method that returns IContext object
                Method method = klass.getMethod("makeContext", Map.class);
                LOG.debug("object:" + obj + " method:" + method);
                transport = (IContext) method.invoke(obj, storm_conf);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Fail to construct messaging plugin from plugin " + transport_plugin_klassName, e);
        }
        return transport;
    }
}

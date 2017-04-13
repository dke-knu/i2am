package org.apache.storm.messaging.jxio;

import org.apache.storm.grouping.Load;
import org.apache.storm.messaging.ConnectionWithStatus;
import org.apache.storm.messaging.IConnectionCallback;
import org.apache.storm.messaging.TaskMessage;
import org.apache.storm.metric.api.IStatefulObject;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by seokwoo on 2017-04-13.
 */
public class Client extends ConnectionWithStatus implements IStatefulObject {
    public Client(Map storm_conf, String host, int port, Context context) {
    }

    @Override
    public Object getState() {
        return null;
    }

    @Override
    public void registerRecv(IConnectionCallback cb) {

    }

    @Override
    public void sendLoadMetrics(Map<Integer, Double> taskToLoad) {

    }

    @Override
    public void send(int taskId, byte[] payload) {

    }

    @Override
    public void send(Iterator<TaskMessage> msgs) {

    }

    @Override
    public Map<Integer, Load> getLoad(Collection<Integer> tasks) {
        return null;
    }

    @Override
    public void close() {

    }

    @Override
    public Status status() {
        return null;
    }
}

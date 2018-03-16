package org.fields.window.grouping.to_be.custom;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.storm.generated.GlobalStreamId;
import org.apache.storm.grouping.CustomStreamGrouping;
import org.apache.storm.task.WorkerTopologyContext;
import org.apache.storm.tuple.Fields;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public class MyPartialKeyGrouping implements CustomStreamGrouping, Serializable {
    private static final long serialVersionUID = -447379837314000353L;
    private List<Integer> targetTasks;
    private long[] targetTaskStats;
    private HashFunction h1 = Hashing.murmur3_128(13);
    private HashFunction h2 = Hashing.murmur3_128(17);
    private Fields fields = null;
    private Fields outFields = null;

    public MyPartialKeyGrouping() {
        //Empty
    }

    public MyPartialKeyGrouping(Fields fields) {
    	System.out.println("construction");
        this.fields = fields;
    }

    @Override
    public void prepare(WorkerTopologyContext context, GlobalStreamId stream, List<Integer> targetTasks) {
    	System.out.println("prepare");
    	for (Integer i : targetTasks)
    		System.out.println(i);
        this.targetTasks = targetTasks;
        targetTaskStats = new long[this.targetTasks.size()];
        if (this.fields != null) {
            this.outFields = context.getComponentOutputFields(stream);
        }
    }

    @Override
    public List<Integer> chooseTasks(int taskId, List<Object> values) {
    	System.out.println("chooseTasks");
    	System.out.println(taskId);
    	for (String s: outFields) {
    		System.out.println(s);
    	}
        List<Integer> boltIds = new ArrayList<>(1);
        if (values.size() > 0) {
            byte[] raw;
            if (fields != null) {
                List<Object> selectedFields = outFields.select(fields, values);
                ByteBuffer out = ByteBuffer.allocate(selectedFields.size() * 4);
                for (Object o: selectedFields) {
                    if (o instanceof List) {
                        out.putInt(Arrays.deepHashCode(((List)o).toArray()));
                    } else if (o instanceof Object[]) {
                        out.putInt(Arrays.deepHashCode((Object[])o));
                    } else if (o instanceof byte[]) {
                        out.putInt(Arrays.hashCode((byte[]) o));
                    } else if (o instanceof short[]) {
                        out.putInt(Arrays.hashCode((short[]) o));
                    } else if (o instanceof int[]) {
                        out.putInt(Arrays.hashCode((int[]) o));
                    } else if (o instanceof long[]) {
                        out.putInt(Arrays.hashCode((long[]) o));
                    } else if (o instanceof char[]) {
                        out.putInt(Arrays.hashCode((char[]) o));
                    } else if (o instanceof float[]) {
                        out.putInt(Arrays.hashCode((float[]) o));
                    } else if (o instanceof double[]) {
                        out.putInt(Arrays.hashCode((double[]) o));
                    } else if (o instanceof boolean[]) {
                        out.putInt(Arrays.hashCode((boolean[]) o));
                    } else if (o != null) {
                        out.putInt(o.hashCode());
                    } else {
                      out.putInt(0);
                    }
                }
                raw = out.array();
            } else {
                raw = values.get(0).toString().getBytes(); // assume key is the first field
            }
            int firstChoice = (int) (Math.abs(h1.hashBytes(raw).asLong()) % this.targetTasks.size());
            int secondChoice = (int) (Math.abs(h2.hashBytes(raw).asLong()) % this.targetTasks.size());
            int selected = targetTaskStats[firstChoice] > targetTaskStats[secondChoice] ? secondChoice : firstChoice;
            boltIds.add(targetTasks.get(selected));
            targetTaskStats[selected]++;
        }
        return boltIds;
    }
}

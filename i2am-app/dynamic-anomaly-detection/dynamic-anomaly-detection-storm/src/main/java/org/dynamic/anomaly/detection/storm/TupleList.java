package org.dynamic.anomaly.detection.storm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.storm.tuple.Tuple;

public class TupleList implements Serializable {

    private final List<Tuple> list = new ArrayList<Tuple>();

    public TupleList() {
    }

    public List<Tuple> getList() {
        return list;
    }

    public void addObject(Tuple o) {
        list.add(o);
    }
    
    public int size() {
    	return list.size();
    }
    
    public void remove(int index) {
    	list.remove(index);
    }
}
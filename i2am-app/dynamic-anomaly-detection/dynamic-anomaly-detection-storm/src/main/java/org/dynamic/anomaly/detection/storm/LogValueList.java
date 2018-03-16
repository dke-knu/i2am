package org.dynamic.anomaly.detection.storm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LogValueList implements Serializable {

    private final List<Double> list = new ArrayList<Double>();

    public LogValueList() {
    }

    public List<Double> getList() {
        return list;
    }

    public void addObject(Double o) {
        list.add(o);
    }
    
    public int size() {
    	return list.size();
    }
    
    public void remove(int index) {
    	list.remove(index);
    }
}
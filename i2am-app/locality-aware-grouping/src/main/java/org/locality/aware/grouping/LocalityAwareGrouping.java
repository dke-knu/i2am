package org.locality.aware.grouping;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.storm.cluster.ClusterUtils;
import org.apache.storm.generated.Assignment;
import org.apache.storm.generated.GlobalStreamId;
import org.apache.storm.generated.NodeInfo;
import org.apache.storm.grouping.CustomStreamGrouping;
import org.apache.storm.task.WorkerTopologyContext;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class LocalityAwareGrouping implements CustomStreamGrouping, Serializable {
	private final static Logger logger = LoggerFactory.getLogger(LocalityAwareGrouping.class);
	private String zookeeper_connect_string;
	private Map<NodeConnection, Set<Long>> node_tasks;

	public LocalityAwareGrouping(String zookeeper_connect_string) {
		this.zookeeper_connect_string = zookeeper_connect_string;
	}

	@Override
	public void prepare(WorkerTopologyContext context, GlobalStreamId stream, List<Integer> targetTasks) {
		// Get network connection information of tasks.
		ZooKeeper zookeeper = null;
		try {
			zookeeper = new ZooKeeper(zookeeper_connect_string, 20000, null);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		
		boolean isConnectedZookeeper = false;
		while (!isConnectedZookeeper) {
			try {
				byte[] zookeeper_data = zookeeper.getData("/storm/assignments/"+context.getStormId(), true, null);
				Assignment assignment = ClusterUtils.maybeDeserialize(zookeeper_data, Assignment.class);
				Map<String, String> node_host = assignment.get_node_host();
				Map<List<Long>, NodeInfo> node_ports = assignment.get_executor_node_port();
				node_tasks = new HashMap<>();
				for (List<Long> tasks: node_ports.keySet()) {
					NodeInfo nodeInfo = node_ports.get(tasks);
					NodeConnection nodeConnection = 
							new NodeConnection(node_host.get(nodeInfo.get_node()), nodeInfo.get_port().iterator().next());
					for (Long task: tasks) {
						if (!targetTasks.contains(task.intValue()))	continue;
						Set<Long> set_task = node_tasks.get(nodeConnection);
						if (set_task == null)
							set_task = new HashSet<>();
						set_task.add(task);
						node_tasks.put(nodeConnection, set_task);
					}
				}
				isConnectedZookeeper = true;
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			} catch (KeeperException ke) {
				ke.printStackTrace();
			}
		}
		
		Thread t = new Thread(new PingThread());
		t.start();
	}

	@Override
	public List<Integer> chooseTasks(int taskId, List<Object> values) {
		NodeConnection picked_node = 
				(NodeConnection) sampleWithReplacement(new ArrayList<>(node_tasks.keySet()));
		Set<Long> picked_tasks = node_tasks.get(picked_node);
		
		Long picked_task = 0L;
		int item = new Random().nextInt(picked_tasks.size());
		int i = 0;
		for(Long task: picked_tasks) {
		    if (i == item) {
		    	picked_task = task;
		    	break;
		    }
		    i++;
		}
		return Lists.newArrayList(new Integer(picked_task.intValue()));
	}

	private interface Sample {
		public double getProbability();
	}
	
	private class NodeConnection implements Sample {
		String host;
		long port;
		double response_time = 0;
		double probability;

		public NodeConnection (String host, long port) {
			this.host = host;
			this.port = port;
		}

		@Override
		public int hashCode() { 
	        return (host+port).hashCode(); 
	    }
		
		@Override
	    public boolean equals(Object obj) {
	    	NodeConnection target = (NodeConnection) obj;
	        return (this.host.equals(target.host) && this.port == target.port);
	    }
		
	    public int compare(Object obj) {
	    	NodeConnection target = (NodeConnection) obj;
	        return this.response_time > target.response_time ? 1 : 
	        	(this.response_time < target.response_time ? -1 : 0);
	    }

		@Override
		public double getProbability() {
			return probability;
		}
		
		public void setProbability(double probability) {
			this.probability = probability;
		}
	}
	
	public Sample sampleWithReplacement(List<Sample> samples) {
		Sample ret = null;
		double random_double = new Random().nextDouble();
		for (Sample sample: samples) {
			random_double -= sample.getProbability();
			if (random_double <= 0) {
				ret = sample;
				break;
			}
		}
		
		return ret;
	}

	private class PingThread implements Runnable {
		@Override
		public void run() {
			while (true) {
				Map<NodeConnection, Set<Long>> node_tasks_tmp = node_tasks;
				
				double sum_of_response_time = 0;
				for (NodeConnection node: node_tasks_tmp.keySet()) {
					node.response_time = doPing(node.host)/node_tasks_tmp.get(node).size();
					sum_of_response_time += 1/node.response_time;
				}
				for (NodeConnection node: node_tasks_tmp.keySet()) {
					node.setProbability(1/(node.response_time*sum_of_response_time));
				}
				
				node_tasks = node_tasks_tmp;
				
				try {Thread.sleep(1*1000);}
				catch (InterruptedException e) {}
			}
		}

		private double doPing(String host) {
			double response_time = 0;
			
		    String[] command = new String[] {"/bin/sh", "-c",
		    		"ping " + host + " -c 1 | grep 'time=' | awk '{print substr($8,6)}'"};
		    try {
				String s = null;
				StringBuilder sb = new StringBuilder();
	
				StringBuilder tq = new StringBuilder();
				for (String ss: command) {
					tq.append(ss);
				}
				ProcessBuilder pb = new ProcessBuilder(command);
				Process process = pb.start();
	
				BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
	
				while ((s = stdInput.readLine()) != null) {
					sb.append(s);
				}
				
				response_time = Double.parseDouble(sb.toString().trim());
				if (response_time <= 0)	response_time=0.0000001;
		    } catch (Exception e) {logger.info(e.getMessage());}
			return response_time;
		}
	}
}
package i2am.tta.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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

public class LocalityAwareGrouping2 implements CustomStreamGrouping, Serializable {
	private final static Logger logger = LoggerFactory.getLogger(LocalityAwareGrouping2.class);
	private String zookeeper_connect_string;
	private List<NodeConnection> nodes;
	private List<Double> probabilities;

	public LocalityAwareGrouping2(String zookeeper_connect_string) {
		this.zookeeper_connect_string = zookeeper_connect_string;
	}

	@Override
	public void prepare(WorkerTopologyContext context, GlobalStreamId stream, List<Integer> targetTasks) {
		// Get network information of tasks.
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
				nodes = new ArrayList<>();
				for (List<Long> tasks: node_ports.keySet()) {
					NodeInfo nodeInfo = node_ports.get(tasks);
					NodeConnection nodeConnection = 
							new NodeConnection(node_host.get(nodeInfo.get_node()), nodeInfo.get_port().iterator().next());
					for (Long task: tasks) {
						if (!targetTasks.contains(task.intValue()))	continue;
						if (!nodes.contains(nodeConnection)) nodes.add(nodeConnection);
						Set<Long> set_task = nodeConnection.getTask();
						if (set_task == null) {
							set_task = new HashSet<>();
							nodeConnection.setTask(set_task);
						}
						set_task.add(task);
					}
				}
				isConnectedZookeeper = true;
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			} catch (KeeperException ke) {
				ke.printStackTrace();
			}
		}
		probabilities = new ArrayList<>(nodes.size());
		for (int i=0; i<nodes.size(); i++) {
			probabilities.add(1.0/nodes.size());
		}
		
		Thread t = new Thread(new PingThread());
		t.start();
	}

	@Override
	public List<Integer> chooseTasks(int taskId, List<Object> values) {
		NodeConnection picked_node = nodes.get(sampleWithReplacement(probabilities));
		Set<Long> picked_tasks = picked_node.getTask();
		
		Long picked_task = 0L;
		if (picked_tasks.size() == 1)
			picked_task = picked_tasks.iterator().next();
		else {
			int item = new Random().nextInt(picked_tasks.size());
			int i = 0;
			for(Long task: picked_tasks) {
			    if (i == item) {
			    	picked_task = task;
			    	break;
			    }
			    i++;
			}
		}
		return Lists.newArrayList(new Integer(picked_task.intValue()));
	}

	private class NodeConnection {
		private String host;
		private Long port;
		private Set<Long> task;
		private double response_time = 0;

		public NodeConnection (String host, long port) {
			this.host = host;
			this.port = port;
		}
		
		public Set<Long> getTask () {
			return this.task;
		}
		
		public void setTask (Set<Long> task) {
			this.task = task;
		}
		
		@Override
		public String toString() {
			return this.host + ":" + this.port + ":" + this.task;
		}
		
		@Override
		public boolean equals (Object object) {
		    boolean result = false;
		    if (object == null || object.getClass() != getClass()) {
		        result = false;
		    } else {
		    	NodeConnection node = (NodeConnection) object;
		        if (this.host.equals(node.host) && this.port.equals(node.port)) {
		            result = true;
		        }
		    }
		    return result;
		}
	}
	
	private int sampleWithReplacement(List<Double> probabilities) {
		int ret = -1;
		double random_double = new Random().nextDouble();
		for (int i=0; i<probabilities.size(); i++) {
			random_double -= probabilities.get(i);
			if (random_double <= 0) {
				ret = i;
				break;
			}
		}
		
		return ret;
	}

	private class PingThread implements Runnable {
		@Override
		public void run() {
			while (true) {
				List<Double> new_probabilities = new ArrayList<>(nodes.size());
				
				double sum_of_response_time = 0;
				for (NodeConnection node: nodes) {
					node.response_time = doPing(node.host)/node.getTask().size();
					sum_of_response_time += 1/node.response_time;
				}
				for (NodeConnection node: nodes) {
					new_probabilities.add(1/(node.response_time*sum_of_response_time));
				}
				
				probabilities = new_probabilities;
				
				try {Thread.sleep(0*1000);}
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
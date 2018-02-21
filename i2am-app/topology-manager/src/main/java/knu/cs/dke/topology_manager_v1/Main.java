package knu.cs.dke.topology_manager_v1;

import java.util.LinkedList;
import java.util.Queue;

public class Main {

	public static void main(String[] args) {
		Queue<String> qCommands = new LinkedList<String>();
		new Thread(new CommandServer(qCommands)).start();
		new Thread(new CommandHandler(qCommands)).start();
	}

}

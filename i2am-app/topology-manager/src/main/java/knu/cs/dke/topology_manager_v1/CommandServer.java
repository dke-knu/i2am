package knu.cs.dke.topology_manager_v1;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;

public class CommandServer implements Runnable {
	private ServerSocket serverSocket = null;
	private final int SERVER_PORT = 11111;
	private Queue<String> qCommands = null;

	public CommandServer(Queue<String> qCommands) {
		this.qCommands = qCommands;
		try {
			serverSocket = new ServerSocket(SERVER_PORT);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		while (true) {
			try {
				Socket socket = serverSocket.accept();
				// TODO

				
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

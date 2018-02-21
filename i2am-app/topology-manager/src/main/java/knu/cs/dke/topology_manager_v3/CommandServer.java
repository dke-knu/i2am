package knu.cs.dke.topology_manager_v3;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommandServer implements Runnable {
	
	// Server Info.
	protected int          serverPort   = 11111;
	protected ServerSocket serverSocket = null; 
	protected boolean      isStopped    = false;
	
	// Client Socket Thread Pool.
	protected ExecutorService threadPool = Executors.newCachedThreadPool();

	// Plan Info.
	private PlanList plans = null;
	private SourceList sources = null;
	private DestinationList destination = null;
	
	// Server Start.
	public static void main(String[] args) {
		new Thread(new CommandServer()).start();		
	}

	public CommandServer(){
		plans = PlanList.getInstance();		
		sources = SourceList.getInstance();
		destination = DestinationList.getInstance();
	}

	public void run(){		
		openServerSocket();
		System.out.println("[Server] Server started ~");
		
		while(!isStopped()){
			Socket clientSocket = null;
			try {
				clientSocket = this.serverSocket.accept();				
			} catch (IOException e) {
				if(isStopped()) {
					System.out.println("[Server] Server Stopped.") ;
					return;
				}
				throw new RuntimeException("[Server] Error accepting client connection", e);
			}
			this.threadPool.execute(new CommandClientSocket(clientSocket, plans, sources, destination));			
		}
		this.threadPool.shutdown();
		System.out.println("[Server] Server Stopped.") ;
	}

	private synchronized boolean isStopped() {
		return this.isStopped;
	}

	public synchronized void stop(){
		this.isStopped = true;
		try {
			this.serverSocket.close();
		} catch (IOException e) {
			throw new RuntimeException("[Server] Error closing server", e);
		}
	}

	private void openServerSocket() {
		try {
			this.serverSocket = new ServerSocket(this.serverPort);
		} catch (IOException e) {
			throw new RuntimeException("[Server] Cannot open port " + this.serverPort, e);
		}
	}
}

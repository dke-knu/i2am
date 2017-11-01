package knu.cs.dke.topology_manager_v3;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommandServerSocket implements Runnable {
	
	protected int          serverPort   = 11111;
	protected ServerSocket serverSocket = null; // server host...
	protected boolean      isStopped    = false;
	//    protected Thread       runningThread= null;
	protected ExecutorService threadPool = Executors.newCachedThreadPool();

	private PlanList plans = null; //
	
	public static void main(String[] args) {
		new Thread(new CommandServerSocket()).start();		
	}

	public CommandServerSocket(){
		plans = PlanList.getInstance();		
	}

	public void run(){
		//        synchronized(this){
		//            this.runningThread = Thread.currentThread();
		//        }
		openServerSocket();
		System.out.println("[Server] Server started ~");
		
		while(! isStopped()){
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
			this.threadPool.execute(new CommandClientSocket(clientSocket, plans));			
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

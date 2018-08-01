package knu.cs.dke.topology_manager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.storm.thrift.transport.TTransportException;

import knu.cs.dke.topology_manager.handlers.DbAdapter;

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
	private DestinationList destinations = null;
	
	// Server Start.
	public static void main(String[] args) {
		new Thread(new CommandServer()).start();		
	}

	public CommandServer(){
		
		plans = PlanList.getInstance();		
		sources = SourceList.getInstance();
		destinations = DestinationList.getInstance();
				
		System.out.println("[Command Server] 데이터베이스를 읽는 중...");
				
		DbAdapter.getInstance().loadSources(sources);
		DbAdapter.getInstance().loadDestinations(destinations);
		try {
			DbAdapter.getInstance().loadPlans(plans);
		} catch (TTransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		plans.printSummary();
		sources.printSummary();
		destinations.printSummary();
	}

	public void run(){		
		openServerSocket();
		printLogo();
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
			this.threadPool.execute(new CommandClientSocket(clientSocket, plans, sources, destinations));			
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
		
	private void printLogo() {
		
		System.out.println(" ");
		System.out.println("      ______   ______    ______   __       __ ");
		System.out.println("     /      | /      \\  /      \\ /  \\     /  |");
		System.out.println("     $$$$$$/ /$$$$$$  |/$$$$$$  |$$  \\   /$$ |");
		System.out.println("       $$ |  $$____$$ |$$ |__$$ |$$$  \\ /$$$ |");
		System.out.println("       $$ |   /    $$/ $$    $$ |$$$$  /$$$$ |");
		System.out.println("       $$ |  /$$$$$$/  $$$$$$$$ |$$ $$ $$/$$ |");
		System.out.println("      _$$ |_ $$ |_____ $$ |  $$ |$$ |$$$/ $$ |");
		System.out.println("     / $$   |$$       |$$ |  $$ |$$ | $/  $$ |");
		System.out.println("     $$$$$$/ $$$$$$$$/ $$/   $$/ $$/      $$/ ");				
		System.out.println("     =========================================");
		System.out.println("             Plan Manager v.2.18.07.19       ");
		System.out.println("     =========================================");
		System.out.println(" ");
		
	}
}

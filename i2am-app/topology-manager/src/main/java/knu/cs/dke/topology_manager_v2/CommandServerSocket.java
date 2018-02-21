package knu.cs.dke.topology_manager_v2;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import knu.cs.dke.topology_manager_v2_test.TestClient2CreatePlan;

public class CommandServerSocket implements Runnable {
    protected int          serverPort   = 11111;
    protected ServerSocket serverSocket = null;
    protected boolean      isStopped    = false;
//    protected Thread       runningThread= null;
    protected ExecutorService threadPool =
        Executors.newCachedThreadPool();
    
    private PlanList plans = null;
    
    public static void main(String[] args) {
    	new Thread(new CommandServerSocket()).start();
    	
    	// for test.
    	for (int i=0; i<10; i++)
    		new Thread(new TestClient2CreatePlan()).start();
    }

    public CommandServerSocket(){
    	plans = PlanList.getInstance();
    }

    @Override
    public void run(){
//        synchronized(this){
//            this.runningThread = Thread.currentThread();
//        }
        openServerSocket();
        while(! isStopped()){
            Socket clientSocket = null;
            try {
                clientSocket = this.serverSocket.accept();
            } catch (IOException e) {
                if(isStopped()) {
                    System.out.println("Server Stopped.") ;
                    return;
                }
                throw new RuntimeException(
                    "Error accepting client connection", e);
            }
            this.threadPool.execute(
                new CommandClientSocket(clientSocket, plans)
                );
        }
        this.threadPool.shutdown();
        System.out.println("Server Stopped.") ;
    }


    private synchronized boolean isStopped() {
        return this.isStopped;
    }

    public synchronized void stop(){
        this.isStopped = true;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

    private void openServerSocket() {
        try {
            this.serverSocket = new ServerSocket(this.serverPort);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open port " + this.serverPort, e);
        }
    }
}

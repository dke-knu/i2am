package knu.cs.dke.topology_manager_v3;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.json.simple.parser.ParseException;

public class CommandClientSocket implements Runnable {

    protected Socket clientSocket = null;
    private PlanList plans = null;

    public CommandClientSocket(Socket clientSocket, PlanList plans) {
        this.clientSocket = clientSocket;
        this.plans = plans;
    }
    
    public void run() {
        try {
            InputStream input  = clientSocket.getInputStream();
            OutputStream output = clientSocket.getOutputStream();
            DataInputStream read = new DataInputStream(input);
            
            // TODO
            String commandJSON = read.readUTF();
            System.out.println("[Client-Socket] Received Command: " +commandJSON);
            
            try {
				String ret = new CommandHandler(plans).executeCommand(commandJSON);
				
			} catch (ParseException e) {
				// TODO
				e.printStackTrace();
			}
            
            read.close();
            output.close();
            input.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
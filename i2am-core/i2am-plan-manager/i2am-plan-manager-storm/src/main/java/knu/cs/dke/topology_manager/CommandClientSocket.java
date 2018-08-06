package knu.cs.dke.topology_manager;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.NotAliveException;
import org.apache.storm.thrift.TException;
import org.json.simple.parser.ParseException;

public class CommandClientSocket implements Runnable {

    protected Socket clientSocket = null;
    private PlanList plans = null;
    private SourceList sources = null;
    private DestinationList destinations = null;
    
    public CommandClientSocket(Socket clientSocket, PlanList plans, SourceList sources, DestinationList destinations) {
        this.clientSocket = clientSocket;
        this.plans = plans;
        this.sources = sources;
        this.destinations = destinations;        		
    }
    
    // Client로 부터 Json을 받아서 Command Handler 호출!!
    public void run() {
        try {
            InputStream input  = clientSocket.getInputStream();
            OutputStream output = clientSocket.getOutputStream();
            DataInputStream read = new DataInputStream(input);            
            
            String commandJSON = read.readUTF();
            System.out.println("[Client Socket] Received Command: " + commandJSON);
            
            try {
				String ret = new CommandHandler(plans, sources, destinations).executeCommand(commandJSON);
				
			} catch (ParseException | InterruptedException e) {
				// TODO
				e.printStackTrace();
			} catch (NotAliveException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (AuthorizationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TException e) {
				// TODO Auto-generated catch block
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
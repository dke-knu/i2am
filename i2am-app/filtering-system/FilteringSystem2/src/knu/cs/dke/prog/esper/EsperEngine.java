package knu.cs.dke.prog.esper;

import java.io.IOException;

import org.apache.log4j.BasicConfigurator;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;

import knu.cs.dke.prog.StreamFileReader;
import knu.cs.dke.prog.StreamTwitterInput;
import knu.cs.dke.prog.util.Constant;
import knu.cs.dke.prog.util.GaussianDistribution;
import knu.cs.dke.prog.util.HashFunction;
import knu.cs.dke.prog.util.ResultFileWriter;
import knu.cs.dke.prog.util.filter.BayesianFilter;
import knu.cs.dke.prog.util.filter.BloomFilter;
import knu.cs.dke.vo.*;

//esper engine
public class EsperEngine {
	public static EPServiceProvider service = null;
	public void start() throws Exception{
		Configuration config = null;
		EPStatement stat = null;
		Listener listener = null;
		EPRuntime runtime = null;
		
		//start
		System.out.println("start?");
		BasicConfigurator.configure();
		config = new Configuration();
		config.addImport(HashFunction.class.getName());
		config.addImport(BloomFilter.class.getName());
		config.addImport(BayesianFilter.class.getName());
		Constant.BayesianResult.clear();
		System.out.println("Esper BayesianResult size: "+Constant.BayesianResult.size());
		if(Constant.Dataset.equals("Network")){			//네트워크

		} else if(Constant.Dataset.equals("Twitter")){	//트위터
			config.addEventType("TwitterEvent",TwitterEvent.class.getName());

		} else{			//가우시안
			config.addEventType("GaussianEvent",GaussianEvent.class.getName());
		}
		service = EPServiceProviderManager.getDefaultProvider(config);
		stat = service.getEPAdministrator().createEPL(Constant.EPL);
		listener = new Listener();
		stat.addListener(listener);

		runtime = service.getEPRuntime();

		//start
		if(Constant.InputType.equals("input_file")){
			//file input
			StreamFileReader streamFileReader = new StreamFileReader();
			streamFileReader.read(Constant.Dataset, runtime);
		}else{
			//api input
			if(Constant.Dataset.equals("Twitter")){
				StreamTwitterInput streamTwitterInput = new StreamTwitterInput();
				Constant.StreamTwitterInput = streamTwitterInput;
				Constant.StreamTwitterInput.start(runtime);		
			}else{
				//Gaussian
				GaussianDistribution gaussianInput = new GaussianDistribution(20, runtime);
				gaussianInput.random();
			}
		}
		
		
		//end
		System.out.println("end");
		if(!Constant.Algorithm.equals("bayesian")){
		Constant.BroadCaster.onClose(Constant.UserSession);
		}
		System.out.println("session close");
		service.destroy();
		
	}
}

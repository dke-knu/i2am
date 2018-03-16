package knu.cs.dke.prog.util;

import java.util.Random;

import com.espertech.esper.client.EPRuntime;

import knu.cs.dke.vo.GaussianEvent;

import java.util.ArrayList;

public class GaussianDistribution {

   private int i, size = 0;
   private EPRuntime runtime = null;
   private ArrayList<Double> gaussianArray = new ArrayList<Double>();
   
   private Random gRandom = new Random();
   
   public GaussianDistribution(int size, EPRuntime runtime){
      this.size = size;
      this.runtime = runtime;
   }
   
   public void random() throws InterruptedException{
	   while(true){
		   for(i = 0; i < size; i++){ 
			   double gaussianData = gRandom.nextGaussian();
//			   if(i%1000 == 0){
			   runtime.sendEvent(new GaussianEvent(gaussianData));
//			   }
		   }
		   Thread.sleep(1000);
	   }

   }
   public double getGaussianNum(int i){
      return gaussianArray.get(i);
   }
}
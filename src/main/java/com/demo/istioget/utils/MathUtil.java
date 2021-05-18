package com.demo.istioget.utils;

import java.util.ArrayList;
import java.util.PriorityQueue;

public class MathUtil {

	public static Double getAvg(ArrayList<Double> array) {
		Double sum = 0.0;
		
		for(int i=4;i<array.size()-5;i++) {
			sum += array.get(i);
			
		}
     	return sum/Math.max(1,array.size()-10);
	}
	
	public static Double getSigma(ArrayList<Double> array, Double avg) {
		Double sigma = 0.0;
		
		for(int i=4;i<array.size()-5;i++)  {
			sigma += (array.get(i)-avg)*(array.get(i)-avg);
		}	
		sigma = Math.sqrt(sigma)/Math.max(1,array.size()-10);
		return sigma;
	}
}
 
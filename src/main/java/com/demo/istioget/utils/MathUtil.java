package com.demo.istioget.utils;

import java.util.ArrayList;
import java.util.PriorityQueue;

public class MathUtil {

	public static Double getAvg(PriorityQueue<Double> q) {
		Double sum = 0.0;
		Object[] array = q.toArray();
		for(int i=4;i<array.length-5;i++) {
			sum += (Double)array[i];
		}
		return sum/Math.max(1,array.length-10);
	}
	
	public static Double getSigma(PriorityQueue<Double> q, Double avg) {
		Double sigma = 0.0;
		Object[] array = q.toArray();
		for(int i=4;i<array.length-5;i++)  {
			sigma += ((Double)array[i]-avg)*((Double)array[i]-avg);
		}	
		sigma = Math.sqrt(sigma)/Math.max(1,array.length-10);
		return sigma;
	}
}
 
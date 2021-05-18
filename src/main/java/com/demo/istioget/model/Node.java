package com.demo.istioget.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.security.PermitAll;

import javafx.util.Pair;

public class Node {
    public class downstream{
        String ds_id="";
        String ds_service="";
        String ds_protocol="";
        Double percentreq=0.0;
        Double rate=0.0;
         
        public downstream(String id,String svc,String pro,Double per,Double rate){
            this.ds_id=id;
            this.ds_service=svc;
            this.ds_protocol=pro;
            this.percentreq=per;
            this.rate=rate;
        }
    }
    private String id="";
    private String service="";
    private String namespace="";
    private String type = "";
    private Double pearson = 0.0;
    private Double perSum = 0.0;
    private Double throughput = 0.0;
    private Double latency_now = 0.0;
    private Double latency_avg = 0.0;
    private boolean root = false;
    
    private Map<String,Pair<Double,Double>> traffic=new HashMap<>();
    private List<downstream> downstreams=new ArrayList<>();
   // private Map<

    

    public Node(String id){
        this.id=id;
    }

    public void setService(String service)
    {
        this.service=service;
    }

    public void addDownstream(String id,String svc,String pro,Double per,Double rate){
    	this.perSum = per/rate;
        downstreams.add(new downstream(id, svc, pro, per, rate));
    }

    public void setNamespace(String namespace)
    {
        this.namespace=namespace;
    }

    public void addTraffic(String protocol,Double in,Double out)
    {
        traffic.put(protocol,new Pair<>(in,out));
    }
    
    public void setType(String is) {
    	this.type = is;
    }
    
    public void setPearson(Double pearson) {
    	this.pearson = pearson;
    }
    
    public void setPerSum(Double perSum) {
    	this.perSum = perSum;
    }
    
    public void setLatency_now(Double latency_now) {
    	this.latency_now = latency_now;
    }
    
    public void setLatency_avg(Double latency_avg) {
    	this.latency_avg = latency_avg;
    }
    
    public void setThroughput(Double throughput) {
    	this.throughput = throughput;
    }
    
    public String getType() {
    	return this.type;
    }
    
    public String getId(){
        return this.id;
    }

    public String getSerivce(){
        return this.service;
    }

    public String getNamespace(){
        return this.namespace;
    }

    public Double getPearson(){
        return this.pearson;
    }
    
    public Double getPerSum(){
        return this.perSum;
    }

    public Double getThroughPut(){
        return this.throughput;
    }

    public Double getLatency_now(){
        return this.latency_now;
    }

    public Double getLatency_avg(){
        return this.latency_avg;
    }

    public Double getTrafficIn(String protocol){
        if(traffic.containsKey(protocol))
        {
            return traffic.get(protocol).getKey();
        }
        else
            return -1.0;
    }

    public Double getTrafficOut(String protocol){
        if(traffic.containsKey(protocol))
        {
            return traffic.get(protocol).getValue();
        }
        else
            return -1.0;
    }
    
    public ArrayList<String> getDstreamId() {
    	ArrayList<String> list = new ArrayList<>();
    	for(downstream ds:downstreams){
    		list.add(ds.ds_id);
    	}
    	return list;
    }
    
    public HashMap<String, Double> getDstreamRate() {
    	HashMap<String, Double> map = new HashMap<>();
    	for(downstream ds:downstreams){
    		map.put(ds.ds_id, ds.rate);
    	}
    	return map;
    }

    public Map<String,Double> getDsstream(){
        Map<String,Double> Dsstream=new HashMap<>();
        for(downstream ds:downstreams){
            Dsstream.put(ds.ds_service,ds.percentreq);
        }
        return Dsstream;
    }

    public void setRoot() {
    	this.root = true;
    }
    
    public boolean isRoot() {
    	return this.root;
    }
    
}
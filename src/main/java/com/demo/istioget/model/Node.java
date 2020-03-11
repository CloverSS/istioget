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

    public String getId(){
        return this.id;
    }

    public String getSerivce(){
        return this.service;
    }

    public String getNamespace(){
        return this.namespace;
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

    public Map<String,Double> getDsstream(){
        Map<String,Double> Dsstream=new HashMap<>();
        for(downstream ds:downstreams){
            Dsstream.put(ds.ds_service,ds.percentreq);
        }
        return Dsstream;
    }

    
}
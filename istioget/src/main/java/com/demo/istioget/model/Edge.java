package com.demo.istioget.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.util.Pair;

public class Edge {
    private class response{
        String code="";
       // String hosts="";
        Double percentres=0.0;

        public response(String code,Double percentres){
            this.code=code;
            this.percentres=percentres;
        }

        private Double getper(){
            return this.percentres;
        }
    }
    private String id="";
    private String source="";
    private String target="";
    private String protocol="";
    private double percentreq=0.0;
    private double rate=0.0;
    private Map<String,response> responses=new HashMap<>();
    
    public Edge(String id){
        this.id=id;
    }

    public void setsource(String source)
    {
        this.source=source;
    }

    public void settarget(String target)
    {
        this.target=target;
    }

    public void setprotocol(String protocol)
    {
        this.protocol=protocol;
    }

    public void setpercentreq(Double percentreq)
    {
        this.percentreq=percentreq;
    }

    public void setrate(Double rate)
    {
        this.rate=rate;
    }

    public void addresponse(String code,Double percentres)
    {
        this.responses.put(code,new response(code,percentres));
    }

    public String getId(){
        return this.id;
    }

    public String getsource(){
        return this.source;
    }

    public String gettarget(){
        return this.target;
    }

    public String getprotocol(){
        return this.protocol;
    }

    public Double getrate(){
        return this.rate;
    }

    public Double getpercentreq(){
        return this.percentreq;
    }

    public Double getresponse(String code){
        if(responses.containsKey(code)){
            return responses.get(code).getper();
        }
        else{
            return -1.0;
        }
    }
    
}
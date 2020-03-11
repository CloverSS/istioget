package com.demo.istioget.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.demo.istioget.conf.BaseConf;
import com.demo.istioget.model.Node;

class FaultDetection {
    public static void faultdetction(int type,Map<String,Node> nodes) throws Exception {
        if(type==1)
            method_svccap(nodes);
        else if(type==2)
            method_compare(nodes);
        else if(type==3)
            method_svclink(nodes);
    }
    private static void method_svccap(Map<String,Node> nodes){
        try{
        for(Node node:nodes.values()){
            String namespace=node.getNamespace();
            String service=node.getSerivce();
            Double p95=FindLatency.SelectLatency(BaseConf.istio_ip,BaseConf.istio_port,namespace,service,"1m",0.95);
            Double p50=FindLatency.SelectLatency(BaseConf.istio_ip,BaseConf.istio_port,namespace,service,"1m",0.50);
            if(p95/p50>2)
            {
                System.out.println("Service_cap:"+service);
            }
        }}catch(Exception err){
            System.out.println(err);
        }

    }

    private static void method_compare(Map<String,Node> nodes){
        try{
        for(Node node:nodes.values()){
            String namespace=node.getNamespace();
            String service=node.getSerivce();
            Double p95_1=FindLatency.SelectLatency(BaseConf.istio_ip,BaseConf.istio_port,namespace,service,"1m",0.95);
            Double p95_60=FindLatency.SelectLatency(BaseConf.istio_ip,BaseConf.istio_port,namespace,service,"60m",0.95);
            if(p95_1/p95_60>2)
            {
                System.out.println("Service_cap:"+service);
            }
        }}catch(Exception err){
            System.out.println(err);
        }
    }
 
    private static void method_svclink(Map<String,Node> nodes){
        try{
            for(Node node:nodes.values()){
                String namespace=node.getNamespace();
                String service=node.getSerivce();
                Double p95_1=FindLatency.SelectLatency(BaseConf.istio_ip,BaseConf.istio_port,namespace,service,"1m",0.95);
                Double p95_60=FindLatency.SelectLatency(BaseConf.istio_ip,BaseConf.istio_port,namespace,service,"60m",0.95);
                if(p95_1/p95_60>2)
                {
                    Map<String,Double> Dsstream=node.getDsstream();
                    for(Map.Entry<String, Double> entry : Dsstream.entrySet()){
                        String DSservice = entry.getKey();
                        Double DSpercent = entry.getValue();
                        Double DSp95_1=FindLatency.SelectLatency(BaseConf.istio_ip,BaseConf.istio_port,namespace,service,DSservice,"1m",0.95);
                        Double DSp95_60=FindLatency.SelectLatency(BaseConf.istio_ip,BaseConf.istio_port,namespace,service,DSservice,"60m",0.95);
                        p95_1-=DSp95_1*DSpercent;
                        p95_60-=DSp95_60*DSpercent;
                    }
                    if(p95_1/p95_60>2)
                        System.out.println("Service_cap:"+service);
                }
        }catch(Exception err){
            System.out.println(err);
        }
    }
}
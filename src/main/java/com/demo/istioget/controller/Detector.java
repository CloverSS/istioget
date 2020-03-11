package com.demo.istioget.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.demo.istioget.conf.BaseConf;
import com.demo.istioget.model.Node;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;


class DetectTask extends TimerTask {
    private List<String> namespaceList=new ArrayList<>();

    private void parse_namespaces(JSONArray nsJson){
        for(int i=0;i<nsJson.length();i++){
            JSONObject rawNs = nsJson.getJSONObject(i);
            if(rawNs.has("metadata")){
                if(rawNs.getJSONObject("metadata").has("name")||rawNs.getJSONObject("metadata").has("labels")){
                    String name = rawNs.getJSONObject("metadata").getString("name");
                    JSONObject labels = rawNs.getJSONObject("metadata").getJSONObject("label");
                    if(labels.has("istio-injection") && 
                        labels.getString("istio-injection")=="enabled"){
                            namespaceList.add(name);
                    }
                }
            }
        }
    }

    private boolean get_namespaces(String ip,String port){
        String theUrl = "http:"+ip+":"+port+"/api/v1/namespaces";
		RestTemplate restTemplate = new RestTemplate();
		try {
		    ResponseEntity<String> response = restTemplate.getForEntity(theUrl, String.class);
            if(response.hasBody()){
                JSONObject rawJson = new JSONObject(response.getBody());
                if(rawJson.has("items"))
                {
                    parse_namespaces(rawJson.getJSONArray("items"));
                }
                if(!namespaceList.isEmpty())
                    return true;
            }
        }catch(Exception e){
            System.out.println(e);
        }
        return false;
    }

    public void run() {  
        try{
        get_namespaces(BaseConf.istio_ip, BaseConf.k8s_port);
        for(String namespace:namespaceList){
            Map<String,Node> nodes=GraphController.get_nodes(namespace);
            FaultDetection.faultdetction(3, nodes);
        }
        }catch(Exception e){
            System.out.println(e);
        }
    }  
}  

public class Detector{
    public Detector(){
        Timer timer = new Timer();
        timer.schedule(new DetectTask(),30 * 1000);
    }  
}
package com.demo.istioget.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.demo.istioget.model.Edge;
import com.demo.istioget.model.Node;

import org.json.JSONArray;
import org.json.JSONObject;

public class GraphAnalyzeHelper {
	Map<String,Node> NodeMap=new HashMap<>();
	List<Edge> EdgeList=new ArrayList<>();

    public GraphAnalyzeHelper(){
    }

    public Map<String,Node> analyzeJsonGraph(JSONObject graphRes){
        analyzeJsonNode(graphRes);
		analyzeJsonEdge(graphRes);
		transEdgeToNode();
		return NodeMap;
    }

    private void analyzeJsonNode(JSONObject graphRes) {
		try {
			if (graphRes.has("nodes")) {
				JSONArray nodes = graphRes.getJSONArray("nodes");
				for (int i = 0; i < nodes.length(); i++) {
					JSONObject data = nodes.getJSONObject(i).getJSONObject("data");
					String service = "", namespace = "", id = "";
					if (data.has("id")) {
						id = data.getString("id");
						Node node=new Node(id);
						if (data.has("service"))
						{
							service = data.getString("service");
							node.setService(service);
						}
						if (data.has("namespace"))
						{
							namespace = data.getString("namespace");
							node.setNamespace(namespace);
						}
						if (data.has("traffic")) {
							//System.out.println("have traffic");
							JSONArray traffics = data.getJSONArray("traffic");
							for (int j = 0; j < traffics.length(); j++) {
								String protocol = "";
								JSONObject rates = new JSONObject();
								if (traffics.getJSONObject(j).has("rates"))
									rates = traffics.getJSONObject(j).getJSONObject("rates");
								if (traffics.getJSONObject(j).has("protocol"))
								{
									//System.out.println("have protocol");
									protocol = traffics.getJSONObject(j).getString("protocol");
									Double in=new Double(-1.0);
									Double out=new Double(-1.0);
									if(rates.has(protocol+"In"))
										in=rates.getDouble(protocol+"In");
									if(rates.has(protocol+"Out"))
										out=rates.getDouble(protocol+"Out");
									node.addTraffic(protocol, in, out);
								}
							}
						}
						//System.out.println("service: " + service);
						NodeMap.put(id, node);
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Analyze json error");
		}
    }
    
    private void analyzeJsonEdge(JSONObject graphRes) {
		try {
			if (graphRes.has("edges")) {
				JSONArray edges = graphRes.getJSONArray("edges");
				for (int i = 0; i < edges.length(); i++) {
					JSONObject data = edges.getJSONObject(i).getJSONObject("data");
					String source = "", target = "", id = "";
					if (data.has("id")) {
						id = data.getString("id");
						Edge edge=new Edge(id);
						if (data.has("target"))
						{
							target = data.getString("target");
							edge.settarget(target);
						}
						if (data.has("source"))
						{
							source = data.getString("source");
							edge.setsource(source);
						}
						if (data.has("traffic")) {
                            JSONObject traffic = data.getJSONObject("traffic");
                            if(traffic.has("protocol")){
								String protocol=traffic.getString("protocol");
								edge.setprotocol(protocol);
								if(traffic.has("rates"))
								{
									//System.out.println("have edge rates");
									JSONObject rate=traffic.getJSONObject("rates");
									if(rate.has(protocol+"PercentReq"))
									{
										Double percentreq=rate.getDouble(protocol+"PercentReq");
										edge.setpercentreq(percentreq);
									}
									if(rate.has(protocol))
									{
										Double ratepro=rate.getDouble(protocol);
										edge.setrate(ratepro);
									}
								}
							}
							if(traffic.has("responses")){
								JSONObject responses=traffic.getJSONObject("responses");
								Iterator it = responses.keys();
								if(it.hasNext()){
									String code=(String)it.next();
									if(responses.getJSONObject(code).getJSONObject("flags").has("-"))
									{
										Double percentres=responses.getJSONObject(code).getJSONObject("flags").getDouble("-");
										edge.addresponse(code, percentres);
									}
								}
							}
						}	
						EdgeList.add(edge);
					//	System.out.println("edges"+edge.getId());
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Analyze json error");
		}
	}

	private String getServiceName(String id){
		Node node_=NodeMap.get(id);
		return node_.getSerivce();
	}

	private void transEdgeToNode(){
		for(Edge edge:EdgeList){
			NodeMap.get(edge.getsource())
				   .addDownstream(edge.gettarget(), getServiceName(edge.gettarget())
				   , edge.getprotocol(), edge.getpercentreq(), edge.getrate());
		}
	}
}
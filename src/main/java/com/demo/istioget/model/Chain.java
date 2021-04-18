package com.demo.istioget.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.stream.events.Namespace;

import org.json.JSONArray;
import org.json.JSONObject;

public class Chain {
	private static ConcurrentHashMap<String,String> JsonRes = new ConcurrentHashMap<>();
	
	public static String getJsonRes(String namespace) {
		return JsonRes.get(namespace);
	}
	
	public static void updata(String namespace, ArrayList<HashMap<String,String>> nodes, ArrayList<HashMap<String,String>> links) {
		JSONArray jsonNodes = new JSONArray();
		JSONArray jsonLinks = new JSONArray();
		for(HashMap<String, String> node : nodes) {
			JSONObject obj = new JSONObject();
			String type = "normal";
			if(node.containsKey("type")&&node.get("type").equals("1"))
				type = "overload";
			else if(node.containsKey("type")&&node.get("type").equals("2"))
				type = "overcapacity";
			obj.put("id", node.containsKey("id")?node.get("id"):"");
			obj.put("name", node.containsKey("name")?node.get("name"):"unknown");
			obj.put("latency60", node.containsKey("latency60")?node.get("latency60"):"");
			obj.put("latency1", node.containsKey("latency1")?node.get("latency1"):"");
			obj.put("podCount", node.containsKey("podCount")?node.get("podCount"):"");
			obj.put("type", type);
			jsonNodes.put(obj);
		}
		
		for(HashMap<String, String> link : links) {
			JSONObject obj = new JSONObject();
			int type = 0;
			obj.put("source", link.containsKey("source")?link.get("source"):"");
			obj.put("target", link.containsKey("target")?link.get("target"):"");
			jsonLinks.put(obj);
		}
		
		JSONObject dataRes = new JSONObject();
		dataRes.put("nodes", jsonNodes);
		dataRes.put("links", jsonLinks);
		JSONObject response = new JSONObject();
		response.put("code", 200);
		response.put("data", dataRes);
		JsonRes.put(namespace, response.toString());
	}
	
}

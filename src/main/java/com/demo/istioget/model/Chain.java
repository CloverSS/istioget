package com.demo.istioget.model;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

public class Chain {
	private static String JsonRes;
	
	public static String getJsonRes() {
		return JsonRes;
	}
	
	public static void updata(ArrayList<HashMap<String,String>> nodes, ArrayList<HashMap<String,String>> links) {
		JSONArray jsonNodes = new JSONArray();
		JSONArray jsonLinks = new JSONArray();
		for(HashMap<String, String> node : nodes) {
			JSONObject obj = new JSONObject();
			int type = 0;
			if(node.containsKey("type")&&node.get("type").equals("1"))
				type = 1;
			obj.put("id", node.containsKey("id")?node.get("id"):"");
			obj.put("name", node.containsKey("name")?node.get("name"):"");
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
		JsonRes = dataRes.toString();
	}
}

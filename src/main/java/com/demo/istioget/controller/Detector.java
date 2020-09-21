package com.demo.istioget.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.demo.istioget.conf.BaseConf;
import com.demo.istioget.model.Node;
import com.demo.istioget.utils.K8sApiClient;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

class DetectTask extends TimerTask {
	//private List<String> namespaceList = new ArrayList<>();

	/*private void parse_namespaces(JSONArray nsJson) {
		for (int i = 0; i < nsJson.length(); i++) {
			JSONObject rawNs = nsJson.getJSONObject(i);
			if (rawNs.has("metadata")) {
				if (rawNs.getJSONObject("metadata").has("name") && rawNs.getJSONObject("metadata").has("labels")) {
					String name = rawNs.getJSONObject("metadata").getString("name");
					System.out.println(name);
					JSONObject labels = rawNs.getJSONObject("metadata").getJSONObject("labels");
					System.out.println(labels);
					if (labels.has("istio-injection") && labels.getString("istio-injection").equals("enabled")) {
						namespaceList.add(name);
						System.out.println(name + "is istio");
					}
				}
			}
		}
	}

	private boolean get_namespaces(String ip, String port) {
		System.out.println("start get namespaces");
		String theUrl = "http://" + ip + ":" + port + "/api/v1/namespaces";
	//	System.out.println(theUrl);
		RestTemplate restTemplate = new RestTemplate();
		try {
			ResponseEntity<String> response = restTemplate.getForEntity(theUrl, String.class);
			if (response.hasBody()) {
				//System.out.println(response.getStatusCode());
				//System.out.println(response.getBody());
				JSONObject rawJson = new JSONObject(response.getBody());
				if (rawJson.has("items")) {
					parse_namespaces(rawJson.getJSONArray("items"));
				}
				if (!namespaceList.isEmpty())
					return true;
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return false;
	}*/

	public void run() {
		try {
			NamespaceList namespaceList= K8sApiClient.getNamespace();
			for (Namespace ns:namespaceList.getItems()) {
				Map<String, Node> nodes = GraphController.get_nodes(ns.getMetadata().getName());
				FaultDetection.faultdetction(3, nodes);
				/*	new Thread() {
						public void run() {
							try {
								FaultDetection.faultdetction(1, nodes);
							} catch (Exception e) {
								System.out.println(e);
							}
						}
					}.start();
					
					new Thread() {
						public void run() {
							try {
								FaultDetection.faultdetction(2, nodes);
							} catch (Exception e) {
								System.out.println(e);
							}
						}
					}.start();
					
					new Thread() {
						public void run() {
							try {
								FaultDetection.faultdetction(3, nodes);
							} catch (Exception e) {
								System.out.println(e);
							}
						}
					}.start();
					
					new Thread() {
						public void run() {
							try {
								FaultDetection.faultdetction(4, nodes);
							} catch (Exception e) {
								System.out.println(e);
							}
						}
					}.start();*/
				
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}

public class Detector {
	public Detector() {

	}

	public void startDetect() {
		Timer timer = new Timer();
		timer.schedule(new DetectTask(), 0, 10 * 1000);
	}
}
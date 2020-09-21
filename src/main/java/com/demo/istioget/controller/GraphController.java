package com.demo.istioget.controller;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.demo.istioget.conf.BaseConf;
import com.demo.istioget.model.Chain;
import com.demo.istioget.model.Node;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class GraphController {
	
	@RequestMapping("/")
	public String home() {
		return "this is istioget";
	}
	
	@CrossOrigin
	@RequestMapping("/chain")
	public String chain(@RequestParam String namespace) {
		return Chain.getJsonRes(namespace);
	}

	public static Map<String,Node> get_nodes(String namespace) throws Exception {
		System.out.println("into graph "+namespace);
		JSONObject graphJson = getGraphFromKiali(namespace);
		System.out.println(graphJson.get("elements").toString());
		JSONObject graphRes = graphJson.getJSONObject("elements");
		GraphAnalyzeHelper analyzer=new GraphAnalyzeHelper();
		Map<String,Node> nodes=analyzer.analyzeJsonGraph(graphRes);
		return nodes;
	}

	/*
	 * "data": { "service": "adservice", "namespace": "hipster-is", "id":
	 * "bd8cbaea90bd81009701a348380ad00f", "nodeType": "service", "destServices": [{
	 * "namespace": "hipster-is", "name": "adservice" }], "traffic": [{ "protocol":
	 * "grpc", "rates": { "grpcOut": "1.51", "grpcIn": "1.51" } }] } }
	 */
	

	 private static JSONObject getGraphFromKiali(String namespace) {
		String theUrl = "http://"+BaseConf.istio_ip+":"+BaseConf.istio_port+"/kiali/api/namespaces/graph?graphType=service&namespaces="+namespace;
		RestTemplate restTemplate = new RestTemplate();
		try {
			HttpHeaders headers = createHttpHeaders("admin", "admin");
			HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
			ResponseEntity<String> response = restTemplate.exchange(theUrl, HttpMethod.GET, entity, String.class);
			JSONObject graphJson = new JSONObject(response.getBody());
			return graphJson;
		} catch (Exception eek) {
			System.out.println("** Exception: " + eek.getMessage());
			return null;
		}
	}

	static private HttpHeaders createHttpHeaders(String user, String password) {
		String notEncoded = user + ":" + password;
		String encodedAuth = Base64.getEncoder().encodeToString(notEncoded.getBytes());
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Authorization", "Basic " + encodedAuth);
		return headers;
	}

}
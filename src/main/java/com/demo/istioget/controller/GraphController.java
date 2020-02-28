package com.demo.istioget.controller;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.demo.istioget.model.Node;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class GraphController {
	
	@RequestMapping("/")
	public String home() {
		return "this is istioget";
	}

	@RequestMapping("/graph")
	public String graph() {
		System.out.println("into graph");
		JSONObject graphJson = getGraphFromKiali();
		System.out.println(graphJson.get("elements").toString());
		JSONObject graphRes = graphJson.getJSONObject("elements");
		GraphAnalyzeHelper analyzer=new GraphAnalyzeHelper();
		analyzer.analyzeJsonGraph(graphRes);
		return graphRes.toString();
	}

	/*
	 * "data": { "service": "adservice", "namespace": "hipster-is", "id":
	 * "bd8cbaea90bd81009701a348380ad00f", "nodeType": "service", "destServices": [{
	 * "namespace": "hipster-is", "name": "adservice" }], "traffic": [{ "protocol":
	 * "grpc", "rates": { "grpcOut": "1.51", "grpcIn": "1.51" } }] } }
	 */
	

	private JSONObject getGraphFromKiali() {
		String theUrl = "http://129.28.142.81:6105/kiali/api/namespaces/graph?graphType=service&namespaces=hipster-is";
		RestTemplate restTemplate = new RestTemplate();
		try {
			HttpHeaders headers = createHttpHeaders("admin", "admin");
			HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
			ResponseEntity<String> response = restTemplate.exchange(theUrl, HttpMethod.GET, entity, String.class);
			JSONObject graphJson = new JSONObject(response.getBody());
			System.out.println("Result - status (" + response.getStatusCode() + ") has body: " + response.hasBody());
			// System.out.println(response.getBody());
			// System.out.println(graphJson.toString());
			return graphJson;
		} catch (Exception eek) {
			System.out.println("** Exception: " + eek.getMessage());
			return null;
		}
	}

	private HttpHeaders createHttpHeaders(String user, String password) {
		String notEncoded = user + ":" + password;
		String encodedAuth = Base64.getEncoder().encodeToString(notEncoded.getBytes());
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Authorization", "Basic " + encodedAuth);
		return headers;
	}

}
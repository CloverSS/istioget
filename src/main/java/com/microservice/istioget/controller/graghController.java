package com.microservice.istioget.controller;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

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
public class graghController {
	 @RequestMapping("/gragh")
	 public JSONObject gragh() {
		 JSONObject graghJson=getGraghFromKiali();
		 System.out.println(graghJson.get("elements").toString());
		 return graghJson;
	 }
	 
	 private JSONObject getGraghFromKiali(){
		 
		 String theUrl = "http://192.168.6.200:30913/kiali/api/namespaces/graph?graphType=service&namespaces=hipster-is";
		 RestTemplate restTemplate = new RestTemplate();
		 try {
		     HttpHeaders headers = createHttpHeaders("admin","admin");
	         HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
	         ResponseEntity<String> response = restTemplate.exchange(theUrl, HttpMethod.GET, entity, String.class);
	         JSONObject graghJson=new JSONObject(response.getBody());
	         System.out.println("Result - status ("+ response.getStatusCode() + ") has body: " + response.hasBody());
	         //System.out.println(response.getBody());
	       //  System.out.println(graghJson.toString());
	         return graghJson;
         }
	     catch (Exception eek) {
	        System.out.println("** Exception: "+ eek.getMessage());
	        return null;
	     }
	}
	 
	 private HttpHeaders createHttpHeaders(String user, String password)
	 {
	     String notEncoded = user + ":" + password;
	     String encodedAuth = Base64.getEncoder().encodeToString(notEncoded.getBytes());
	     HttpHeaders headers = new HttpHeaders();
	     headers.setContentType(MediaType.APPLICATION_JSON);
	     headers.add("Authorization", "Basic " + encodedAuth);
	     return headers;
	 }
	 
	 
}

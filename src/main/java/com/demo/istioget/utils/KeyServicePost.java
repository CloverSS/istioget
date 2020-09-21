package com.demo.istioget.utils;

import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.demo.istioget.conf.BaseConf;

class KeyService {
	String svcName;
	String namespace;

	public KeyService(String namespace, String svcName) {
		this.svcName = svcName;
		this.namespace = namespace;
	}

	public String getSvcName() {
		return this.svcName;
	}

	public String getNamespace() {
		return this.namespace;
	}
}

public class KeyServicePost {

	private static ConcurrentHashMap<KeyService, Long> opeMap = new ConcurrentHashMap<>();

	public static void putKeyService(String namespace, String svcName, Long timestamp) {
		KeyService keyservice = new KeyService(namespace, svcName);
		if (!opeMap.containsKey(keyservice) || timestamp - opeMap.get(keyservice) > 5 * 60) {
			opeMap.put(keyservice, timestamp);
			PostToPredict(namespace, svcName);
		}
	}

	private static void PostToPredict(String namespace, String svcName) {
		String theUrl = "http://localhost:" + BaseConf.scaleservice_ip
				+ "/scale?namespace="+namespace+"&svcName="+svcName+"&type=cpu";
		RestTemplate restTemplate = new RestTemplate();
		LinkedMultiValueMap body=new LinkedMultiValueMap<>();
	    body.add("namespace", namespace);
	    body.add("svcName", svcName);
	    body.add("type", "cpu");
	   try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<String> entity = new HttpEntity<String>(headers);
			ResponseEntity<String> response = restTemplate.exchange(theUrl, HttpMethod.POST, entity, String.class);
		} catch (Exception eek) {
			System.out.println("** Exception: " + eek.getMessage());
		}
	}

}

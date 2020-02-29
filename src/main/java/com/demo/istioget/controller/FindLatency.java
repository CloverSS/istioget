package com.demo.istioget.controller;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

class FindLatency {
    static double SelectLatency(String url,String port,String namespace,String source,String destina,String duration,Double percent)
    {
        String query="histogram_quantile("+percent.toString()+",sum(rate(istio_request_duration_seconds_bucket{reporter=\"source\",destination_workload=~\""+destina+"\",source_workload=\""+source+"\", destination_workload_namespace=~\""+namespace+"\"}["+duration+"]))by(le))";
        String theUrl="http://"+url+":"+port+"/api/v1/query?query="+query;
        JSONObject promRes=DoSelect(theUrl);
        if(promRes.has("value"))
        {
            JSONArray promArray=promRes.getJSONArray("value");
            return promArray.getDouble(1);
        }
        return 0.0;
    }

    private static JSONObject DoSelect(String theUrl) {
		//String theUrl = "http://129.28.142.81:6105/kiali/api/namespaces/graph?graphType=service&namespaces=hipster-is";
		RestTemplate restTemplate = new RestTemplate();
		try {
		//	HttpHeaders headers = createHttpHeaders("admin", "admin");
			HttpEntity<String> entity = new HttpEntity<String>("parameters");
            ResponseEntity<String> response = restTemplate.exchange(theUrl, HttpMethod.GET, entity, String.class);
            if(response.hasBody()){
                JSONObject promJson = new JSONObject(response.getBody());
                System.out.println("Result - status (" + response.getStatusCode() + ") has body: " + response.hasBody());
                if(promJson.has("data")){
                    if(promJson.getJSONObject("data").has("result")){
                        JSONArray promRes=promJson.getJSONObject("data").getJSONArray("result");
                        return promRes.getJSONObject(0);
                    }
                }     
            }
			// System.out.println(response.getBody());
			// System.out.println(graphJson.toString());
			return null;
		} catch (Exception eek) {
			System.out.println("** Exception: " + eek.getMessage());
			return null;
		}
	}
}
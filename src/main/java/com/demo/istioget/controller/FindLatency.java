package com.demo.istioget.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

class FindLatency {
    static double SelectLatency(String ip,String port,String namespace,String source,String destina,String duration,Double percent) throws Exception
    {
        String condition="reporter=\"distina\",destination_workload=~\""+destina+"\",source_workload=~\""+source+"\", destination_workload_namespace=~\""+namespace+"\"";
        String query="histogram_quantile("+percent.toString()+",sum(rate(istio_request_duration_seconds_bucket{}["+duration+"]))by(le))";
        String theUrl="http://"+ip+":"+port+"/api/v1/query?query="+query;
        System.out.println(theUrl);
        JSONObject promRes=DoSelect(theUrl,condition);
        if(promRes.has("value"))
        {
            JSONArray promArray=promRes.getJSONArray("value");
            return promArray.getDouble(1);
        }
        return 0.0;
    }

    static double SelectLatency(String ip,String port,String namespace,String destina,String duration,Double percent) throws Exception
    {
        String condition="reporter=\"destina\",destination_workload=~\""+destina+"\",destination_workload_namespace=~\""+namespace+"\"";
        String query="histogram_quantile("+percent.toString()+",sum(rate(istio_request_duration_seconds_bucket{}["+duration+"]))by(le))";
        String theUrl="http://"+ip+":"+port+"/api/v1/query?query="+query;
        System.out.println(theUrl);
        JSONObject promRes=DoSelect(theUrl,condition);
        if(promRes.has("value"))
        {
            JSONArray promArray=promRes.getJSONArray("value");
            return promArray.getDouble(1);
        }
        return 0.0;
    }

    private static JSONObject DoSelect(String theUrl,String condition) {
		//String theUrl = "http://129.28.142.81:6105/kiali/api/namespaces/graph?graphType=service&namespaces=hipster-is";
		RestTemplate restTemplate = new RestTemplate();
		try {
		    //HttpHeaders headers = createHttpHeaders("admin", "admin");
			//HttpEntity<String> entity = new HttpEntity<String>("parameters");
            ResponseEntity<String> response = restTemplate.getForEntity(theUrl, String.class, condition);
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
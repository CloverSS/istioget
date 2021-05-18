package com.demo.istioget.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

class FindLatency {
	static double SelectQps(String ip, String port, String namespace, String service_name, String duration) {
		String condition = "destination_service_name=~\"" + service_name + "\",reporter=\"destination\", destination_service_namespace=~\""
				+ namespace + "\"";
		String query = "round(sum(irate(istio_requests_total{" + condition + "}[" + duration + "])),0.01)";
		String theUrl = "http://" + ip + ":" + port + "/api/v1/query?query=" + query;
		JSONObject promRes = DoSelect(theUrl);
		//System.out.println(theUrl);
		if (promRes.has("value")) {
			JSONArray promArray = promRes.getJSONArray("value");
			if (promArray.getString(1).equals("NaN")) {

			} else {
				Double res = promArray.getDouble(1);
				// System.out.println(res);
				return res;
			}
		}
		return 0.0;
	}
	
	static ArrayList<Double> SelectQpsList(String ip, String port, String namespace, String service_name, String duration, long time) {
		ArrayList<Double> Res = new ArrayList<Double>();
		String condition = "destination_service_name=~\"" + service_name + "\",reporter=\"destination\",destination_service_namespace=~\""
				+ namespace + "\"";
		String query = "round(sum(irate(istio_requests_total{" + condition + "}[" + duration + "])),0.01)";
		String theUrl = "http://" + ip + ":" + port + "/api/v1/query_range?query=" + query + "&start="
				+ (time - 60 * 10) + "&end=" + time + "&step=30";
		JSONObject promRes = DoSelect(theUrl);
		//System.out.println(theUrl);
		try {
			if (promRes.has("values")) {
				JSONArray promArray = promRes.getJSONArray("values");
				for (int i = 0; i < promArray.length(); i++) {
					JSONArray value = promArray.getJSONArray(i);
					Res.add(value.getDouble(1));
				}
			}
		} catch (Exception err) {
			System.out.println(err);
			System.out.println(promRes.toString());
		}
		return Res;
	}

	static double SelectMaxQps(String ip, String port, String namespace, String service_name, String duration) {
		String condition = "destination_service_name=~\"" + service_name + "\",destination_service_namespace=~\""
				+ namespace + "\"";
		String query = "max_over_time(round(sum(irate(istio_requests_total{" + condition + "}[1m])),0.01)[" + duration
				+ ":])";
		String theUrl = "http://" + ip + ":" + port + "/api/v1/query?query=" + query;
		System.out.println(theUrl);
		JSONObject promRes = DoSelect(theUrl);
		if (promRes.has("value")) {
			JSONArray promArray = promRes.getJSONArray("value");
			if (promArray.getString(1).equals("NaN")) {

			} else {
				Double res = promArray.getDouble(1);
				// System.out.println(res);
				return res;
			}
		}
		return 0.0;
	}

	static double SelectLatency(String ip, String port, String namespace, String source, String destina,
			String duration, Double percent, long time) throws Exception {
		String condition = "reporter=\"destination\",destination_workload=~\"" + destina + "\",source_workload=~\""
				+ source + "\", destination_workload_namespace=~\"" + namespace + "\"";
		String query = "histogram_quantile(" + percent.toString() + ",sum(rate(istio_request_duration_seconds_bucket{}["
				+ duration + "]))by(le))";
		String theUrl = "http://" + ip + ":" + port + "/api/v1/query?query=" + query + "&time=" + time;
		JSONObject promRes = DoSelect(theUrl);
		// System.out.println(promRes.toString());
		if (promRes.has("value")) {
			JSONArray promArray = promRes.getJSONArray("value");
			if (promArray.getString(1).equals("NaN")) {

			} else {
				Double res = promArray.getDouble(1);
				// System.out.println(res);
				return res;
			}
		}
		return 0.0;
	}

	static double SelectAvgLatency(String ip, String port, String namespace, String destina, String duration,
			Double percent, long time) throws Exception {
		String condition = "reporter=\"destination\",destination_workload=~\"" + destina
				+ "\", destination_workload_namespace=~\"" + namespace + "\"";
		String query = "sum_over_time(histogram_quantile(" + percent.toString()
				+ ",sum(rate(istio_request_duration_seconds_bucket{" + condition + "}[1m]))by(le))[" + duration
				+ ":])/count_over_time(histogram_quantile(" + percent.toString()
				+ ",sum(rate(istio_request_duration_seconds_bucket{" + condition + "}[1m]))by(le))[" + duration + ":])";
		String theUrl = "http://" + ip + ":" + port + "/api/v1/query?query=" + query + "&time=" + time;
		// System.out.println(theUrl);
		JSONObject promRes = DoSelect(theUrl);
		// System.out.println(promRes.toString());
		if (promRes.has("value")) {
			JSONArray promArray = promRes.getJSONArray("value");
			if (promArray.getString(1).equals("NaN")) {

			} else {
				Double res = promArray.getDouble(1);
				// System.out.println(res);
				return res;
			}
		}
		return 0.0;
	}

	static double SelectLatency(String ip, String port, String namespace, String destina, String duration,
			Double percent, long time) throws Exception {
		String condition = "reporter=\"destination\",destination_workload=~\"" + destina
				+ "\",destination_workload_namespace=~\"" + namespace + "\"";
		String query = "histogram_quantile(" + percent.toString() + ",sum(rate(istio_request_duration_seconds_bucket{"
				+ condition + "}[" + duration + "]))by(le))";
		String theUrl = "http://" + ip + ":" + port + "/api/v1/query?query=" + query + "&time=" + time;
		JSONObject promRes = DoSelect(theUrl);
		try {
			if (promRes.has("value")) {
				JSONArray promArray = promRes.getJSONArray("value");
				if (promArray.getString(1).equals("NaN")) {

				} else {
					Double res = promArray.getDouble(1);
					return res;
				}
			}
		} catch (Exception err) {
			System.out.println(err);
			System.out.println(promRes.toString());
		}
		return 0.0;
	}

	static ArrayList<Double> SelectLatencyList(String ip, String port, String namespace, String destina,
			String duration, Double percent, long time) throws Exception {
		ArrayList<Double> Res = new ArrayList<Double>();
		String condition = "reporter=\"destination\",destination_workload=~\"" + destina
				+ "\",destination_workload_namespace=~\"" + namespace + "\"";
		String query = "histogram_quantile(" + percent.toString() + ",sum(rate(istio_request_duration_seconds_bucket{"
				+ condition + "}[" + duration + "]))by(le))";
		String theUrl = "http://" + ip + ":" + port + "/api/v1/query_range?query=" + query + "&start="
				+ (time - 60 * 60) + "&end=" + time + "&step=30";
		JSONObject promRes = DoSelect(theUrl);
		try {
			if (promRes.has("values")) {
				JSONArray promArray = promRes.getJSONArray("values");
				for (int i = 0; i < promArray.length(); i++) {
					JSONArray value = promArray.getJSONArray(i);
					if(!value.get(1).equals("NaN"))
						Res.add(value.getDouble(1));
				}
			}
		} catch (Exception err) {
			System.out.println(err);
			System.out.println(promRes.toString());
		}
		return Res;
	}
	
	static String SelectPodCount(String ip, String port, String Namespace, String Service, Long StartTime, Long EndTime) {
		JSONObject dataRes = new JSONObject();
		JSONArray jsonTime = new JSONArray();
		JSONArray jsonNum = new JSONArray();
        String query = "sum(kube_pod_info{created_by_name=~\"" + Service + ".*\", namespace=~\"" + Namespace + "\"})";
        String theUrl = "http://" + ip + ":" + port + "/api/v1/query_range?query=" + query + "&start="
                + StartTime + "&end=" + EndTime + "&step=30s";

        JSONObject promRes = DoSelect(theUrl);
        SimpleDateFormat Dateformat =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //设置格式
       // System.out.println(promRes.toString());
        if (promRes.has("values")) {
            JSONArray promArray = promRes.getJSONArray("values");
            for (int i = 0; i < promArray.length(); i++) {
                JSONArray datapoint = promArray.getJSONArray(i);
                Long timePoint = datapoint.getLong(0);
                String numpoint = datapoint.getString(1);
                jsonTime.put(Dateformat.format(timePoint*1000));
                jsonNum.put(numpoint);
            }
        } 
        dataRes.put("time", jsonTime);
        dataRes.put("num", jsonNum);
        return dataRes.toString();
    }
	
	private static JSONObject DoSelect(String theUrl) {
		// String theUrl =
		// "http://129.28.142.81:6105/kiali/api/namespaces/graph?graphType=service&namespaces=hipster-is";
		RestTemplate restTemplate = new RestTemplate();
		try {
			// HttpHeaders headers = createHttpHeaders("admin", "admin");
			// HttpEntity<String> entity = new HttpEntity<String>("parameters");
			UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(theUrl);
			UriComponents uriComponents = builder.build();
			ResponseEntity<String> response = restTemplate.getForEntity(uriComponents.toUri(), String.class);
			// System.out.println(response);
			// ResponseEntity<String> response = restTemplate.getForEntity(theUrl,
			// String.class, condition);
			if (response.hasBody()) {
				JSONObject promJson = new JSONObject(response.getBody());
				// System.out.println("Result - status (" + response.getStatusCode() + ") has
				// body: " + response.hasBody());
				if (promJson.has("data")) {
					if (promJson.getJSONObject("data").has("result")) {
						JSONArray promRes = promJson.getJSONObject("data").getJSONArray("result");
						if (promRes.isEmpty())
							return new JSONObject();
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
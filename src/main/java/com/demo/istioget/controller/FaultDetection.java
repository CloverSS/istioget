package com.demo.istioget.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.demo.istioget.conf.BaseConf;
import com.demo.istioget.model.Node;
import com.demo.istioget.utils.K8sApiClient;
import com.demo.istioget.utils.KeyServicePost;
import com.demo.istioget.model.Chain;

class FaultDetection {
	public static void faultdetction(int type, Map<String, Node> nodes) throws Exception {
		if (type == 1)
			method_svccap(nodes);
		else if (type == 2)
			method_compare(nodes);
		else if (type == 3)
			method_svclink(nodes);
		else if (type == 4)
			method_complink(nodes);
	}

	private static void method_svccap(Map<String, Node> nodes) {
		try {
			for (Node node : nodes.values()) {
				String namespace = node.getNamespace();
				String service = node.getSerivce();
				Date day = new Date();
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				long timenow = (new Date().getTime()) / 1000;
				Double p90 = FindLatency.SelectLatency(BaseConf.istio_ip, BaseConf.prom_port, namespace, service, "1m",
						0.90, timenow);
				Double p50 = FindLatency.SelectLatency(BaseConf.istio_ip, BaseConf.prom_port, namespace, service, "1m",
						0.50, timenow);
				if (p90 / p50 > 2) {
					System.out.println("time " + df.format(day) + " namespace: " + namespace
							+ " method_svccap Service_cap:" + service);
				}
			}
		} catch (Exception err) {
			System.out.println(err);
		}

	}

	private static void method_compare(Map<String, Node> nodes) {
		try {
			for (Node node : nodes.values()) {
				String namespace = node.getNamespace();
				String service = node.getSerivce();
				Date day = new Date();
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				long timenow = new Date().getTime();
				Double p90_1 = FindLatency.SelectLatency(BaseConf.istio_ip, BaseConf.prom_port, namespace, service,
						"1m", 0.90, timenow);
				Double p90_60 = FindLatency.SelectLatency(BaseConf.istio_ip, BaseConf.prom_port, namespace, service,
						"60m", 0.90, timenow - 60);
				System.out.println("time: " + df.format(day) + " namespace: " + namespace + " Service_raw:" + service
						+ " p90_1:" + p90_1 + " p90_60：" + p90_60);
				if (p90_1 / p90_60 > 2) {
					// Date day=new Date();
					// SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					System.out.println("time: " + df.format(day) + " namespace: " + namespace
							+ " method_compare Service_cap:" + service);
				}
			}
		} catch (Exception err) {
			System.out.println(err);
		}
	}

	private static void method_svclink(Map<String, Node> nodes) {
		try {
			Date day = new Date();
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			ArrayList<HashMap<String, String>> tojson_nodes = new ArrayList<>();
			ArrayList<HashMap<String, String>> links = new ArrayList<>();
			String namespace = "";
			for (Node node : nodes.values()) {
				namespace = node.getNamespace();
				String service = node.getSerivce();
				long timenow = (new Date().getTime()) / 1000;
				Double p90_1 = FindLatency.SelectLatency(BaseConf.istio_ip, BaseConf.prom_port, namespace, service,
						"1m", 0.90, timenow);
				Double p90_60 = FindLatency.SelectAvgLatency(BaseConf.istio_ip, BaseConf.prom_port, namespace, service,
						"60m", 0.90, timenow);
				
				Double qps_1m = FindLatency.SelectQps(BaseConf.istio_ip, BaseConf.prom_port, namespace, service,
						"1m");
				Double qps_60m = FindLatency.SelectMaxQps(BaseConf.istio_ip, BaseConf.prom_port, namespace, service,
						"10m");
				System.out.println("time: " + df.format(day) + " namespace: " + namespace + " Service_raw:" + service
						+ " qps_1m:" + qps_1m + " qps_60m：" + qps_60m);
				Double latencyp90_1 = p90_1;
				Double latencyp90_60 = p90_60;
				//System.out.println("time: " + df.format(day) + " namespace: " + namespace + " Service_raw:" + service
				//		+ " p90_1:" + p90_1 + " p90_60：" + p90_60);

				Map<String, Double> Dsstream = node.getDsstream();
				for (Map.Entry<String, Double> entry : Dsstream.entrySet()) {
					String DSservice = entry.getKey();
					Double DSpercent = entry.getValue()*0.01;
					Double DSp90_1 = FindLatency.SelectLatency(BaseConf.istio_ip, BaseConf.prom_port, namespace,
							service, DSservice, "1m", 0.90, timenow);
					Double DSp90_60 = FindLatency.SelectLatency(BaseConf.istio_ip, BaseConf.prom_port, namespace,
							service, DSservice, "60m", 0.90, timenow);
					p90_1 -= DSp90_1 * DSpercent;
					p90_60 -= DSp90_60 * DSpercent;
				}
				if (p90_1 / p90_60 > 2 && latencyp90_1 / latencyp90_60 > 2) {
					System.out.println("Type1 time " + df.format(day) + " namespace: " + namespace
							+ " method_svclink Service overload:" + service);
					
					node.setType("1");
				}else if(qps_60m/qps_1m > 1.2) {
					System.out.println("Type2 time " + df.format(day) + " namespace: " + namespace
							+ " method_svclink Service Overcapacity:" + service);
					node.setType("2");
				}
				
				if(node.getType().equals("1")||node.getType().equals("2")) {
					System.out.println("put key service : type ="+node.getType()+node.getSerivce());
					KeyServicePost.putKeyService(namespace, node.getSerivce(), 0.0, 2, timenow);
				}
				
				HashMap<String, String> nodemap = new HashMap<>();
				nodemap.put("id", node.getId());
				nodemap.put("name", node.getSerivce().equals("")?"unknown":node.getSerivce());
				nodemap.put("latency1", String.format("%.2f", latencyp90_1));
				nodemap.put("latency60", String.format("%.2f", latencyp90_60));
				nodemap.put("type", node.getType());
				nodemap.put("podCount", K8sApiClient.getReplicas(namespace, service).toString());
				tojson_nodes.add(nodemap);

				for (String target : node.getDstreamId()) {
					HashMap<String, String> linkmap = new HashMap<>();
					linkmap.put("source", node.getId());
					linkmap.put("target", target);
					links.add(linkmap);
				}
			}
			//Chain.updata(namespace, tojson_nodes, links);
		} catch (Exception err) {
			System.out.println(err);
		}
	}

	private static void method_complink(Map<String, Node> nodes) {
		try {
			Date day = new Date();
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			for (Node node : nodes.values()) {
				String namespace = node.getNamespace();
				String service = node.getSerivce();
				long timenow = (new Date().getTime()) / 1000;
				Double p90 = FindLatency.SelectLatency(BaseConf.istio_ip, BaseConf.prom_port, namespace, service, "1m",
						0.90, timenow);
				Double p50 = FindLatency.SelectLatency(BaseConf.istio_ip, BaseConf.prom_port, namespace, service, "1m",
						0.50, timenow);
				System.out.println("time: " + df.format(day) + " namespace: " + namespace + " Service_raw:" + service
						+ " p90:" + p90 + " p50: " + p50);
				if (p90 / p50 > 2) {
					Map<String, Double> Dsstream = node.getDsstream();
					for (Map.Entry<String, Double> entry : Dsstream.entrySet()) {
						String DSservice = entry.getKey();
						Double DSpercent = entry.getValue();
						Double DSp90 = FindLatency.SelectLatency(BaseConf.istio_ip, BaseConf.prom_port, namespace,
								service, DSservice, "1m", 0.90, timenow);
						Double DSp50 = FindLatency.SelectLatency(BaseConf.istio_ip, BaseConf.prom_port, namespace,
								service, DSservice, "1m", 0.50, timenow);
						p90 -= DSp90 * DSpercent;
						p50 -= DSp50 * DSpercent;
					}  
					if (p90 / p50 > 2)
						System.out.println("time " + df.format(day) + " namespace: " + namespace
								+ " method_complink Service_cap:" + service);
				}
			}
		} catch (Exception err) {
			System.out.println(err);
		}
	}
}
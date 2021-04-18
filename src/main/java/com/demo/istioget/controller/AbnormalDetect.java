package com.demo.istioget.controller;

import java.text.SimpleDateFormat;
import java.util.PriorityQueue;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.demo.istioget.conf.BaseConf;
import com.demo.istioget.model.Chain;
import com.demo.istioget.model.Node;
import com.demo.istioget.utils.K8sApiClient;
import com.demo.istioget.utils.KeyServicePost;
import com.demo.istioget.utils.MathUtil;

public class AbnormalDetect {
	static class AbnormalService {
		String id = "";
		String serviceName = "";
		Double mean = 0.0;
		Double sigma = 0.0;
		Double Pearson = 0.0;
		Double Pearson_W = 0.0;
		Double Pearson_Sum = 0.0;
		Double Call_Sum = 0.0;
		Double throughput = 0.0;

		ArrayList<Double> historyLatency = new ArrayList<>();
		HashMap<String, Double> downStream_rate = new HashMap<>();
		HashMap<String, Double> pearson_weight = new HashMap<>();
		HashMap<String, Double> call_weight = new HashMap<>();

		AbnormalService(String id, String serviceName, Double mean, Double sigma, ArrayList<Double> historyLatency,
				HashMap<String, Double> downStream_rate, Double Call_Sum, Double throughput) {
			this.id = id;
			this.serviceName = serviceName;
			this.mean = mean;
			this.sigma = sigma;
			this.historyLatency = new ArrayList<Double>(
					historyLatency.subList(Math.max(0, historyLatency.size() - 20), historyLatency.size()));
			this.downStream_rate = downStream_rate;
			this.throughput = throughput;
			this.Call_Sum = Call_Sum + throughput;
			System.out.println("serviceName: " + serviceName + " Call_sum: " + this.Call_Sum);
		}
	}

	public static void Detect(Map<String, Node> nodes) {
		try {
			Date day = new Date();
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			ArrayList<HashMap<String, String>> tojson_nodes = new ArrayList<>();
			ArrayList<HashMap<String, String>> links = new ArrayList<>();
			String namespace = "";
			HashMap<String, AbnormalService> AbnormalServices = new HashMap<>();
			String root_id = "";
			long timenow = (new Date().getTime()) / 1000;

			for (Node node : nodes.values()) {
				namespace = node.getNamespace();
				String service = node.getSerivce();

				ArrayList<Double> Res = FindLatency.SelectLatencyList(BaseConf.istio_ip, BaseConf.prom_port, namespace,
						service, "1m", 0.90, timenow - 60);
				Double lat_now = FindLatency.SelectLatency(BaseConf.istio_ip, BaseConf.prom_port, namespace, service,
						"1m", 0.90, timenow);

				PriorityQueue<Double> svcLat = new PriorityQueue<>();
				for (Double lat : Res) {
					svcLat.offer(lat);
				}

				node.setLatency_now(lat_now);
				Double mean = MathUtil.getAvg(svcLat);
				Double sigma = MathUtil.getSigma(svcLat, mean);
				System.out.println("service: " + service + " latecy_now: " + lat_now + " mean: " + mean + " sigma: "
						+ sigma + " uppoint: " + (mean + 3 * sigma));

				//if (!Double.isNaN(mean) && !Double.isNaN(sigma) && lat_now > mean + 3 * sigma) { // 服务性能异常判定
				if (!service.equals("")) {
					AbnormalServices.put(node.getId(), new AbnormalService(node.getId(), service, mean, sigma, Res,
							node.getDstreamRate(), node.getPerSum(), node.getThroughPut()));
					if (node.isRoot()) {
						root_id = node.getId();
					}
					node.setType("1");
				} else if (!Double.isNaN(mean) && !Double.isNaN(sigma)) { // 服务资源过量判定
					ArrayList<Double> Res_t = FindLatency.SelectQpsList(BaseConf.istio_ip, BaseConf.prom_port,
							namespace, service, "1m", timenow);
					Double ThroughPut_now = FindLatency.SelectQps(BaseConf.istio_ip, BaseConf.prom_port, namespace,
							service, "1m");
					
					PriorityQueue<Double> HisThroughPut = new PriorityQueue<>();
					for (Double tp : Res_t) {
						HisThroughPut.offer(tp);
					}
					
					Double mean_t = MathUtil.getAvg(HisThroughPut);
					Double sigma_t = MathUtil.getSigma(HisThroughPut, mean_t);
					System.out.println("service: " + service + " throughput_now: " + ThroughPut_now + " mean: "
							+ mean_t + " sigma: " + sigma_t + " uppoint: " + (mean_t - 3 * sigma_t));
					if (!Double.isNaN(mean_t) && !Double.isNaN(sigma_t) && ThroughPut_now < mean_t) {
						node.setType("3");
						System.out.println("service: " + service + " throughput_now: " + ThroughPut_now + " mean: "
								+ mean_t + " sigma: " + sigma_t + " uppoint: " + (mean_t - 3 * sigma_t));
						KeyServicePost.putKeyService(namespace, service, timenow);
					}
				}

			}

			if (!root_id.equals("")) { // 服务过载上报
				calPearson(AbnormalServices, root_id);
				String Rootcause_id = MagicPageRank(AbnormalServices, root_id);
				nodes.get(Rootcause_id).setType("2");
				System.out.println(Rootcause_id + " serviceName:" + nodes.get(Rootcause_id).getSerivce());
				KeyServicePost.putKeyService(namespace, nodes.get(Rootcause_id).getSerivce(), timenow);
			}

			for (Node node : nodes.values()) { // 更新传递给前端数据
				String service = node.getSerivce();
				HashMap<String, String> nodemap = new HashMap<>();
				nodemap.put("id", node.getId());
				nodemap.put("name", node.getSerivce().equals("") ? "unknown" : node.getSerivce());
				nodemap.put("latency1", node.getLatency_now().toString());
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
			Chain.updata(namespace, tojson_nodes, links);
		} catch (Exception err) {
			System.out.println(err);
		}
	}

	static void calPearson(HashMap<String, AbnormalService> AbnormalServices, String root_id) {
		AbnormalService rootService = AbnormalServices.get(root_id);
		ArrayList<Double> RootServiceLatency = rootService.historyLatency;

		for (AbnormalService service : AbnormalServices.values()) {
			double deno1 = 0.0;
			double deno2 = 0.0;
			double num = 0.0;
			ArrayList<Double> serviceLatency = service.historyLatency;
			for (int i = 0; i < Math.min(serviceLatency.size(), RootServiceLatency.size()); i++) {
				deno1 += (serviceLatency.get(i) - service.mean) * (serviceLatency.get(i) - service.mean);
				deno2 += (RootServiceLatency.get(i) - rootService.mean)
						* (RootServiceLatency.get(i) - rootService.mean);
				num += (serviceLatency.get(i) - service.mean) * (RootServiceLatency.get(i) - rootService.mean);
			}
			service.Pearson = num / (Math.sqrt(deno1) * Math.sqrt(deno2));
			// System.out.println("serviceName: " + service.serviceName + " Pearson: " +
			// service.Pearson);
		}
	}

	static String MagicPageRank(HashMap<String, AbnormalService> AbnormalServices, String root_id) {
		int epoch = 3;
		HashMap<String, double[]> serviceRank = new HashMap<>();
		HashMap<String, double[]> latRank = new HashMap<>();
		HashMap<String, double[]> pearRank = new HashMap<>();
		double a = 0.5;
		double b = 0.5;
		double max_pear = 0.0;
		String Rootcause_id_pear = "";
		for (AbnormalService service : AbnormalServices.values()) {
			if(!Double.isNaN(service.Pearson)) {
			serviceRank.put(service.id, new double[epoch + 1]);
			serviceRank.get(service.id)[0] = Math.abs(service.Pearson);
			
			latRank.put(service.id, new double[epoch + 1]);
			latRank.get(service.id)[0] = Math.abs(service.Pearson);
			
			pearRank.put(service.id, new double[epoch + 1]);
			pearRank.get(service.id)[0] = Math.abs(service.Pearson);
			
			if( Math.abs(service.Pearson)>max_pear) {
				max_pear = Math.abs(service.Pearson);
				Rootcause_id_pear = service.id;	
			}
			

			Double max = 0.0;
			Double sum = 0.0;

			for (Map.Entry<String, Double> entry : service.downStream_rate.entrySet()) {
				String id = entry.getKey();
				if (serviceRank.containsKey(id)) {
					max = Math.max(max, serviceRank.get(id)[0]);
					sum += serviceRank.get(id)[0];
				}
			}
			sum += Math.max(0, serviceRank.get(service.id)[0] - max);
			service.Pearson_W = Math.max(0, service.Pearson - max);
			service.Pearson_Sum = sum;
			}
		}

		for (int i = 0; i < epoch; i++) {
			for (AbnormalService service : AbnormalServices.values()) {

				for (Map.Entry<String, Double> entry : service.downStream_rate.entrySet()) {
					String id = entry.getKey();
					if (serviceRank.containsKey(id)&&!Double.isNaN(AbnormalServices.get(id).Pearson)&&!Double.isNaN(entry.getValue())) {
						serviceRank.get(id)[i + 1] += a * AbnormalServices.get(id).Pearson / service.Pearson_Sum
								* serviceRank.get(service.id)[i];
						serviceRank.get(id)[i + 1] += b * entry.getValue() / service.Call_Sum
								* serviceRank.get(service.id)[i];
						
						pearRank.get(id)[i + 1] += AbnormalServices.get(id).Pearson / service.Pearson_Sum
								* serviceRank.get(service.id)[i];
						latRank.get(id)[i + 1] += entry.getValue() / service.Call_Sum
								* serviceRank.get(service.id)[i];

					}
				}
				serviceRank.get(service.id)[i + 1] += a * service.Pearson_W / service.Pearson_Sum
						+ b * service.throughput / service.Call_Sum;
				
				latRank.get(service.id)[i + 1] += service.throughput / service.Call_Sum;
				
				pearRank.get(service.id)[i + 1] += service.Pearson_W / service.Pearson_Sum;
				// System.out.println(service.serviceName + " " + serviceRank.get(service.id)[i
				// + 1]);
				// System.out.println(service.serviceName + " " + serviceRank.get(service.id)[i
				// + 1]+ " call sum" + service.Call_Sum + " Pearson_Sum" + service.Pearson_Sum);
			}
		}

		String Rootcause_id = "";
		String Rootcause_id_p = "";
		String Rootcause_id_l = "";
		double max = 0.0;
		double max_p = 0.0;
		double max_l = 0.0;
		for (AbnormalService service : AbnormalServices.values()) {
			if (serviceRank.get(service.id)[epoch] > max) {
				max = serviceRank.get(service.id)[epoch];
				
				Rootcause_id = service.id;
			}
			
			if(pearRank.get(service.id)[epoch] > max_p) {
				max_p = pearRank.get(service.id)[epoch];
				Rootcause_id_p = service.id;
			}
			
			if(latRank.get(service.id)[epoch] > max_l) {
				max_l = latRank.get(service.id)[epoch];
				Rootcause_id_l = service.id;
			}
			
		}
		System.out.println(
				"My serviceName: " + AbnormalServices.get(Rootcause_id).serviceName + " serviceRank: " + serviceRank.get(Rootcause_id)[epoch]);
		System.out.println(
				"Pearson serviceName: " + AbnormalServices.get(Rootcause_id_pear).serviceName + " serviceRank: " + AbnormalServices.get(Rootcause_id_pear).Pearson);
		System.out.println(
				"Lat serviceName: " + AbnormalServices.get(Rootcause_id_l).serviceName + " serviceRank: " + latRank.get(Rootcause_id_l)[epoch]);
		System.out.println(
				"Pear serviceName: " + AbnormalServices.get(Rootcause_id_p).serviceName + " serviceRank: " + pearRank.get(Rootcause_id_p)[epoch]);


		return Rootcause_id;
	}
}

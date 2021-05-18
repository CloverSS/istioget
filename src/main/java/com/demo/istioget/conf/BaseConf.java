package com.demo.istioget.conf;

public class BaseConf {
	public static String istio_ip;
	public static String istio_port;
	public static String monitoring_port;
	public static String k8s_port;
	public static String prom_port;
	public static String istio_usr;
	public static String istio_passwd;
	public static String scaleservice_ip;
	private static boolean inited = false;

	// private static instance=
	public BaseConf instance() {
		return this;
	}

	public static boolean init(String ip, String k8s_port, String monitor_port, String istio_port, String prom_port, String usr,
			String passwd, String scaleservice_ip) {
		if (!inited) {
			init_instance(ip, k8s_port,monitor_port, istio_port, prom_port, usr, passwd, scaleservice_ip);
			inited = true;
			return true;
		}
		return false;
	}

	private static void init_instance(String ip, String k8s_p, String monitor_port, String istio_p, String prom_p,
			String usr, String passwd, String scalesvc_ip) {
		istio_ip = ip;
		k8s_port = k8s_p;
		monitoring_port = monitor_port;
		istio_port = istio_p;
		prom_port = prom_p;
		istio_usr = usr;
		istio_passwd = passwd;
		scaleservice_ip = scalesvc_ip;

	}

	private BaseConf() {
	};
}
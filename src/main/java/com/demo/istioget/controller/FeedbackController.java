package com.demo.istioget.controller;

import java.util.Date;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.demo.istioget.conf.BaseConf;
import com.demo.istioget.utils.KeyServicePost;

@RestController
public class FeedbackController {

	@PostMapping("/feedback")
	public void feedback(@RequestParam String namespace, @RequestParam String svcName, @RequestParam Double upPoint) {
		try {
			Thread.sleep(30 * 1000);
			long timenow = (new Date().getTime()) / 1000;
			Double latency_now = FindLatency.SelectLatency(BaseConf.istio_ip, BaseConf.prom_port, namespace, svcName, "1m",
					0.90, timenow);
			if(latency_now > upPoint) {
				KeyServicePost.putKeyService(namespace, svcName, upPoint, 2, timenow);
			}
		} catch (Exception err) {
			System.out.println(err);
		}
	}
}

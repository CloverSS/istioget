package com.demo.istioget;

import com.demo.istioget.conf.BaseConf;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.demo.istioget.controller.*;
import com.demo.istioget.utils.K8sApiClient;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceList;
@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		System.out.println("start");
		SpringApplication.run(DemoApplication.class, args);
		BaseConf.init("192.168.6.100","6448","30241","30799","admin","admin","8091");
		Detector detector=new Detector();
		detector.startDetect();
	}

} 
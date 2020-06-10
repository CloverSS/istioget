package com.demo.istioget;

import com.demo.istioget.conf.BaseConf;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.demo.istioget.controller.*;
@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
		BaseConf.init("192.168.6.18","6448","32018","31458","admin","admin");
		Detector detector=new Detector();
		detector.startDetect();
	}

} 
package com.demo.istioget.utils;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import io.fabric8.kubernetes.api.model.NamespaceList;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

public class K8sApiClient {
	
	@ConfigurationProperties("k8s")
	public static class K8sProperties {
	    private String url;
	    @Value("admin-conf")
	    private Resource adminConf;
	    
	    Resource getAdminConf() {
	    	return adminConf;
	    }
	}
	
	private static KubernetesClient client;
	static {
		Resource adminConf = new FileSystemResource("D:/Users/licr/eclipse-workspace/istioget/src/main/resources/admin.conf");
		try {
			String adminConfData = IOUtils.toString(adminConf.getInputStream(),"UTF-8");
			Config config;
			config = Config.fromKubeconfig(adminConfData);
			client = new DefaultKubernetesClient(config);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	public static NamespaceList getNamespace() {
		NamespaceList namespaceList = client.namespaces().withLabel("istio-injection").list();
		return namespaceList;
	}
}

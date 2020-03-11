package com.demo.istioget.conf;
public class BaseConf{
    public static String istio_ip;
    public static String istio_port;
    public static String k8s_port;
    public static String prom_port;
    public static String istio_usr;
    public static String istio_passwd;
    private static boolean inited=false;
   // private static instance=
    public BaseConf instance(){
        return this;
    }
    public static boolean init(String ip,String k8s_port,String istio_port,String prom_port,String usr,String passwd){
        if(!inited){
            init_instance(ip,k8s_port,istio_port,prom_port,usr,passwd);
            inited=true;
            return true;
        }
        return false;
    }
    private static void init_instance(String ip,String k8s_port,String istio_port,String prom_port,String usr,String passwd){
        istio_ip=ip;
        k8s_port=k8s_port;
        istio_port=istio_port;
        prom_port=prom_port;
        istio_usr=usr;
        istio_passwd=passwd;
    }

    private BaseConf(){};
}
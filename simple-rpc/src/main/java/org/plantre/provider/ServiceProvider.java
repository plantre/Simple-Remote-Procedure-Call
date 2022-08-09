package org.plantre.provider;



public interface ServiceProvider {


    <T>void addService(T service,String rpcServiceName);

    Object getService(String rpcServiceName);

    <T>void publishService(T service,String rpcServiceName);
}

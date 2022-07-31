package org.plantre.provider;

import org.plantre.config.RpcServiceConfig;

public interface ServiceProvider {


    void addService(RpcServiceConfig rpcServiceConfig);

    Object getService(String rpcServiceName);

    void publishService(RpcServiceConfig rpcServiceConfig);
}

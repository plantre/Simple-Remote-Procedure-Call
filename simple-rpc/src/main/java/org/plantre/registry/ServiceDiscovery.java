package org.plantre.registry;



import org.plantre.common.entity.RpcRequest;

import java.net.InetSocketAddress;


public interface ServiceDiscovery {

    InetSocketAddress lookupService(String rpcServiceName);
}

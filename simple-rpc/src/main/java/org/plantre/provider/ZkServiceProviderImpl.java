package org.plantre.provider;


import lombok.extern.slf4j.Slf4j;
import org.plantre.common.enumeration.RpcError;
import org.plantre.common.exception.RpcException;
import org.plantre.registry.ZkServiceRegistryImpl;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 */
@Slf4j
public class ZkServiceProviderImpl implements ServiceProvider {

    /**
     * key: rpc服务名字
     * value: rpc服务
     */
    private static final Map<String, Object> serviceMap= new ConcurrentHashMap<>();;
    private final Set<String> registeredService;
    private final ZkServiceRegistryImpl serviceRegistry;

    public ZkServiceProviderImpl() {
        registeredService = ConcurrentHashMap.newKeySet();
        serviceRegistry = new ZkServiceRegistryImpl();
    }

    @Override
    public <T> void addService(T service,String rpcServiceName) {
        if (registeredService.contains(rpcServiceName)) {
            return;
        }
        registeredService.add(rpcServiceName);
        serviceMap.put(rpcServiceName, service);
        log.info("Add service: {} and interfaces:{}", rpcServiceName, service.getClass().getInterfaces());
    }

    @Override
    public Object getService(String rpcServiceName) {
        Object service = serviceMap.get(rpcServiceName);
        if (null == service) {
            throw new RpcException(RpcError.SERVICE_NOT_FOUND);
        }
        return service;
    }

    @Override
    public <T> void publishService(T service,String rpcServiceName) {
        try {
            String host = InetAddress.getLocalHost().getHostAddress();
            this.addService(service,rpcServiceName);
            serviceRegistry.registerService(rpcServiceName, new InetSocketAddress(host, 9998));
        } catch (UnknownHostException e) {
            log.error("occur exception when getHostAddress", e);
        }
    }

}

package org.plantre.registry;

import org.apache.curator.framework.CuratorFramework;
import org.plantre.common.utils.CuratorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class ZkServiceRegistryImpl implements ServiceRegistry{
    private static final Logger logger = LoggerFactory.getLogger(ZkServiceRegistryImpl.class);


    @Override
    public void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress) {
        String servicePath = CuratorUtils.ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName + inetSocketAddress.toString();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        CuratorUtils.createPersistentNode(zkClient, servicePath);
    }

}

package org.plantre;

import org.plantre.annotation.EnableRpcService;
import org.plantre.remoting.dynamicproxy.RpcServer;
import org.plantre.remoting.transport.netty.server.NettyRpcServer;
import org.plantre.serialize.Serializer;


/**
 * Netty服务提供者测试
 */
@EnableRpcService
public class NettyRpcServerTest {

    public static void main(String[] args) {
        RpcServer server = new NettyRpcServer(9999, Serializer.KRYO_SERIALIZER);
        server.start();
    }

}

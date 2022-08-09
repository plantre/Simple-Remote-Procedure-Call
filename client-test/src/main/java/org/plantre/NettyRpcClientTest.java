package org.plantre;


import org.plantre.remoting.dynamicproxy.RpcClient;
import org.plantre.remoting.dynamicproxy.RpcClientProxy;
import org.plantre.remoting.transport.netty.client.NettyRpcClient;
import org.plantre.serialize.Serializer;

/**
 * Netty消费者测试
 */
public class NettyRpcClientTest {

    public static void main(String[] args) {
        RpcClient client = new NettyRpcClient(Serializer.KRYO_SERIALIZER);
        RpcClientProxy rpcClientProxy = new RpcClientProxy(client);
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        String str = helloService.hello(new Hello("Hello", "rpc"));
        System.out.println(str);
    }
}

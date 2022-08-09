package org.plantre;

import org.plantre.remoting.dynamicproxy.RpcClientProxy;
import org.plantre.remoting.transport.socket.client.SocketRpcClient;
import org.plantre.serialize.Serializer;


/**
 * Socket消费者测试
 */
public class SocketRpcClientTest {
    public static void main(String[] args) {
        SocketRpcClient client=new SocketRpcClient(Serializer.DEFAULT_SERIALIZER);
        RpcClientProxy rpcClientProxy = new RpcClientProxy(client);

        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        String str=helloService.hello(new Hello("Hello", "rpc"));
        System.out.println(str);

    }
}

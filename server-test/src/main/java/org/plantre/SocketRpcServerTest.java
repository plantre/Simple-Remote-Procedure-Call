package org.plantre;

import org.plantre.serialize.Serializer;
import org.plantre.serviceimpl.HelloServiceImpl;
import org.plantre.remoting.transport.socket.server.SocketRpcServer;

public class SocketRpcServerTest {
    public static void main(String[] args) {
        HelloService helloService=new HelloServiceImpl();
        SocketRpcServer socketRpcServer=new SocketRpcServer("127.0.0.1", 9998, Serializer.KRYO_SERIALIZER);
        socketRpcServer.start();
    }
}

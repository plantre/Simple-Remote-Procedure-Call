package org.plantre;

import org.plantre.annotation.EnableRpcService;
import org.plantre.serialize.Serializer;
import org.plantre.remoting.transport.socket.server.SocketRpcServer;


@EnableRpcService
public class SocketRpcServerTest {
    public static void main(String[] args) {
        SocketRpcServer socketRpcServer=new SocketRpcServer("127.0.0.1", 10008, Serializer.KRYO_SERIALIZER);
        socketRpcServer.start();
    }
}

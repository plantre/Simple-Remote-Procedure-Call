package org.plantre;

import org.plantre.serviceimpl.HelloServiceImpl;

public class SocketServer {
    public static void main(String[] args) {
        HelloService helloService=new HelloServiceImpl();
        SocketRpcServer socketRpcServer=new SocketRpcServer();
    }
}

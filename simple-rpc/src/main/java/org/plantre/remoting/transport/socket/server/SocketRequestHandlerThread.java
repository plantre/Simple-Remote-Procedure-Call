package org.plantre.remoting.transport.socket.server;

import org.plantre.common.entity.RpcRequest;
import org.plantre.common.factory.SingletonFactory;
import org.plantre.remoting.handler.RpcRequestHandler;
import org.plantre.remoting.transport.socket.codec.MyReader;
import org.plantre.serialize.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SocketRequestHandlerThread implements Runnable{
    private static final Logger log = LoggerFactory.getLogger(SocketRequestHandlerThread.class);

    private Socket socket;
    private RpcRequestHandler rpcRequestHandler;
    private Serializer serializer;

    public SocketRequestHandlerThread(Socket socket,Serializer serializer) {
        this.socket = socket;
        this.rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);;
        this.serializer = serializer;
    }


    @Override
    public void run() {
        try(InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream()) {
            RpcRequest rpcRequest = (RpcRequest) MyReader.readObject(inputStream);
            Object result = rpcRequestHandler.handle(rpcRequest);


        }catch (IOException e){
            log.error("调用或发送出错！", e);
        }


    }

}

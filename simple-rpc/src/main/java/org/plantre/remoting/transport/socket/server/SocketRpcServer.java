package org.plantre.remoting.transport.socket.server;


import lombok.extern.slf4j.Slf4j;
import org.plantre.common.factory.ThreadPoolFactory;

import org.plantre.config.ShutdownHook;
import org.plantre.provider.ServiceProvider;
import org.plantre.provider.ZkServiceProviderImpl;
import org.plantre.registry.ServiceRegistry;
import org.plantre.registry.ZkServiceRegistryImpl;
import org.plantre.serialize.Serializer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

@Slf4j
public class SocketRpcServer {
    private String host;
    private int port;

    private final ExecutorService threadPool;
    private ServiceRegistry serviceRegistry;
    private ServiceProvider serviceProvider;
    private final Serializer serializer;


    public SocketRpcServer(String host, int port, Integer serializer) {
        this.host = host;
        this.port = port;
        threadPool= ThreadPoolFactory.createDefaultThreadPool("socket-rpc-server");
        this.serviceRegistry=new ZkServiceRegistryImpl();
        this.serviceProvider=new ZkServiceProviderImpl();
        this.serializer = Serializer.getByCode(serializer);

    }

    public void start(){
        try(ServerSocket server = new ServerSocket()){
            server.bind(new InetSocketAddress(host,port));
            log.info("服务器正在启动...");
            ShutdownHook.getShutdownHook().addClearAllHook();
            Socket socket;
            while ((socket = server.accept()) != null) {
                log.info("消费者连接: {}:{}", socket.getInetAddress(), socket.getPort());
                threadPool.execute(new SocketRequestHandlerThread(socket,serializer));
            }
            threadPool.shutdown();
        }catch (IOException e){
            log.error("服务器启动失败！",e);
        }
    }

}

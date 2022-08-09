package org.plantre.remoting.transport.socket.server;


import lombok.extern.slf4j.Slf4j;
import org.plantre.annotation.RpcService;
import org.plantre.annotation.EnableRpcService;
import org.plantre.common.enumeration.RpcError;
import org.plantre.common.exception.RpcException;
import org.plantre.common.factory.ThreadPoolFactory;

import org.plantre.common.utils.ReflectUtil;
import org.plantre.config.ShutdownHook;
import org.plantre.provider.ServiceProvider;
import org.plantre.provider.ZkServiceProviderImpl;
import org.plantre.registry.ServiceRegistry;
import org.plantre.registry.ZkServiceRegistryImpl;
import org.plantre.remoting.dynamicproxy.RpcServer;
import org.plantre.serialize.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.Set;
import java.util.concurrent.ExecutorService;

@Slf4j
public class SocketRpcServer implements RpcServer {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    private String host;
    private int port;

    private final ExecutorService threadPool;
    private final ServiceRegistry serviceRegistry;
    private final ServiceProvider serviceProvider;
    private final Serializer serializer;



    public SocketRpcServer(String host, int port, Integer serializer) {
        this.host = host;
        this.port = port;
        threadPool= ThreadPoolFactory.createDefaultThreadPool("socket-rpc-server");
        this.serviceRegistry=new ZkServiceRegistryImpl();
        this.serviceProvider=new ZkServiceProviderImpl();
        this.serializer = Serializer.getByCode(serializer);
        enableRpcServices();

    }

    public void start(){
        try(ServerSocket server = new ServerSocket()){
            String host = InetAddress.getLocalHost().getHostAddress();
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

    public void enableRpcServices() {
        String mainClassName = ReflectUtil.getStackTrace();
        Class<?> startClass;
        try {
            startClass = Class.forName(mainClassName);
            if(!startClass.isAnnotationPresent(EnableRpcService.class)) {
                logger.error("启动类缺少 @RpcServiceScan 注解");
                throw new RpcException(RpcError.SERVICE_SCAN_PACKAGE_NOT_FOUND);
            }
        } catch (ClassNotFoundException e) {
            logger.error("出现未知错误");
            throw new RpcException(RpcError.UNKNOWN_ERROR);
        }
        String basePackage = startClass.getAnnotation(EnableRpcService.class).value();
        if("".equals(basePackage)) {
            basePackage = mainClassName.substring(0, mainClassName.lastIndexOf("."));
        }
        Set<Class<?>> classSet = ReflectUtil.getClasses(basePackage);
        for(Class<?> clazz : classSet) {
            if(clazz.isAnnotationPresent(RpcService.class)) {
                String serviceName = clazz.getAnnotation(RpcService.class).name();
                Object obj;
                try {
                    obj = clazz.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    logger.error("创建 " + clazz + " 时有错误发生");
                    continue;
                }
                if("".equals(serviceName)) {
                    Class<?>[] interfaces = clazz.getInterfaces();
                    for (Class<?> oneInterface: interfaces){
                        publishService(obj, oneInterface.getCanonicalName());
                    }
                } else {
                    publishService(obj, serviceName);
                }
            }
        }
    }

    @Override
    public <T> void publishService(T service, String serviceName) {
        String host = null;
        try {
            host = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        serviceProvider.addService(service, serviceName);
        serviceRegistry.registerService(serviceName, new InetSocketAddress(host, port));
    }

}

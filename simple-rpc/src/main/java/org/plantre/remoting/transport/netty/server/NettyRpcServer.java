package org.plantre.remoting.transport.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.plantre.annotation.RpcService;
import org.plantre.annotation.EnableRpcService;
import org.plantre.common.enumeration.RpcError;
import org.plantre.common.exception.RpcException;
import org.plantre.common.utils.ReflectUtil;
import org.plantre.config.ShutdownHook;
import org.plantre.provider.ServiceProvider;
import org.plantre.provider.ZkServiceProviderImpl;
import org.plantre.registry.ServiceRegistry;
import org.plantre.registry.ZkServiceRegistryImpl;
import org.plantre.remoting.dynamicproxy.RpcServer;
import org.plantre.remoting.transport.netty.codec.MyDecoder;
import org.plantre.remoting.transport.netty.codec.MyEncoder;
import org.plantre.serialize.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class NettyRpcServer implements RpcServer {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());


    private int port;

    private final ServiceRegistry serviceRegistry;
    private final ServiceProvider serviceProvider;
    private final Serializer serializer;



    public NettyRpcServer(int port, Integer serializer) {
        this.port = port;
        this.serviceRegistry=new ZkServiceRegistryImpl();
        this.serviceProvider=new ZkServiceProviderImpl();
        this.serializer = Serializer.getByCode(serializer);
        scanServices();

    }


    @Override
    public void start() {
        ShutdownHook.getShutdownHook().addClearAllHook();
        String host = null;
        try {
            host = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {

            ServerBootstrap serverBootstrap=new ServerBootstrap();
            serverBootstrap.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 256)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new IdleStateHandler(60, 0, 0, TimeUnit.SECONDS))
                                    .addLast(new MyEncoder(serializer))
                                    .addLast(new MyDecoder())
                                    .addLast(new NettyServerHandler());
                        }
                    });
            ChannelFuture future = serverBootstrap.bind(host, port).sync();
            future.channel().closeFuture().sync();
        }catch (InterruptedException e){
            logger.error("启动服务器时有错误发生: ", e);
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }


    }


    public void scanServices() {
        String mainClassName = ReflectUtil.getStackTrace();
        Class<?> startClass;
        try {
            startClass = Class.forName(mainClassName);
            if(!startClass.isAnnotationPresent(EnableRpcService.class)) {
                logger.error("启动类缺少 @EnableRpcService 注解");
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

package org.plantre.remoting.transport.socket.client;

import org.plantre.common.entity.RpcRequest;
import org.plantre.common.entity.RpcResponse;
import org.plantre.common.enumeration.ResponseCode;
import org.plantre.common.enumeration.RpcError;
import org.plantre.common.exception.RpcException;
import org.plantre.common.utils.RpcMessageChecker;
import org.plantre.loadbalance.LoadBalance;
import org.plantre.loadbalance.RandomLoadBalance;
import org.plantre.registry.ServiceDiscovery;
import org.plantre.registry.ZkServiceDiscoveryImpl;
import org.plantre.remoting.dynamicproxy.RpcClient;
import org.plantre.remoting.transport.socket.codec.MyReader;
import org.plantre.remoting.transport.socket.codec.MyWriter;
import org.plantre.serialize.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SocketRpcClient implements RpcClient {

    private static final Logger logger = LoggerFactory.getLogger(SocketRpcClient.class);

    private final ServiceDiscovery serviceDiscovery;
    private final Serializer serializer;

    public SocketRpcClient() {
        this(DEFAULT_SERIALIZER, new RandomLoadBalance());
    }
    public SocketRpcClient(LoadBalance loadBalance) {
        this(DEFAULT_SERIALIZER, loadBalance);
    }
    public SocketRpcClient(Integer serializer) {
        this(serializer, new RandomLoadBalance());
    }


    public SocketRpcClient(Integer serializer, LoadBalance loadBalance) {
        this.serviceDiscovery = new ZkServiceDiscoveryImpl();
        this.serializer = Serializer.getByCode(serializer);
    }


    public Object sendRequest(RpcRequest rpcRequest) {
        if(serializer == null) {
            logger.error("未设置序列化器");
            throw new RpcException(RpcError.SERIALIZER_NOT_FOUND);
        }
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest.getInterfaceName());
        try (Socket socket = new Socket()) {
            socket.connect(inetSocketAddress);
            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();
            MyWriter.writeObject(outputStream, rpcRequest, serializer);
            Object obj = MyReader.readObject(inputStream);
            RpcResponse rpcResponse = (RpcResponse) obj;
            if (rpcResponse == null) {
                logger.error("服务调用失败，service：{}", rpcRequest.getInterfaceName());
                throw new RpcException(RpcError.SERVICE_INVOCATION_FAILURE, " service:" + rpcRequest.getInterfaceName());
            }
            if (rpcResponse.getStatusCode() == null || rpcResponse.getStatusCode() != ResponseCode.SUCCESS.getCode()) {
                logger.error("调用服务失败, service: {}, response:{}", rpcRequest.getInterfaceName(), rpcResponse);
                throw new RpcException(RpcError.SERVICE_INVOCATION_FAILURE, " service:" + rpcRequest.getInterfaceName());
            }
            RpcMessageChecker.check(rpcRequest, rpcResponse);
            return rpcResponse;
        } catch (IOException e) {
            logger.error("调用时有错误发生：", e);
            throw new RpcException("服务调用失败: ", e);
        }
    }

}

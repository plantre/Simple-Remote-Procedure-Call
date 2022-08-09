package org.plantre.remoting.dynamicproxy;


import org.plantre.common.entity.RpcRequest;
import org.plantre.serialize.Serializer;


public interface RpcClient {

    int DEFAULT_SERIALIZER = Serializer.KRYO_SERIALIZER;

    Object sendRequest(RpcRequest rpcRequest);

}

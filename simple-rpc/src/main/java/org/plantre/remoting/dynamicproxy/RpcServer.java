package org.plantre.remoting.dynamicproxy;

import org.plantre.serialize.Serializer;

public interface RpcServer {

    int DEFAULT_SERIALIZER = Serializer.KRYO_SERIALIZER;

    void start();

    <T> void publishService(T service, String serviceName);

}
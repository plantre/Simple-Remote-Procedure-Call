package org.plantre.serviceimpl;

import org.plantre.Hello;
import org.plantre.HelloService;
import org.plantre.annotation.RpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@RpcService
public class HelloServiceImpl implements HelloService {

    private static final Logger logger = LoggerFactory.getLogger(HelloServiceImpl.class);

    @Override
    public String hello(Hello object) {
        logger.info("接收到消息：{}", object.getMessage());
        return "这是Impl方法";
    }

}

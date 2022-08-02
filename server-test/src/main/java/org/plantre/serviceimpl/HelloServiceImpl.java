package org.plantre.serviceimpl;

import lombok.extern.slf4j.Slf4j;
import org.plantre.Hello;
import org.plantre.HelloService;

@Slf4j
public class HelloServiceImpl implements HelloService {

    static {
        System.out.println("HelloServiceImpl被创建");
    }

    @Override
    public String hello(Hello hello) {
        log.info("HelloServiceImpl收到: {}.", hello.getMessage());
        String result = "Description is " + hello.getDescription();
        log.info("HelloServiceImpl返回: {}.", result);
        return result;

    }
}

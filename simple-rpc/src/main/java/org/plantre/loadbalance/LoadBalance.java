package org.plantre.loadbalance;


import java.util.List;

public interface LoadBalance {

    String selectServiceAddress(List<String> serviceUrlList, String rpcServiceName);
}

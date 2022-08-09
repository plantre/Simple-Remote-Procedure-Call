package org.plantre.loadbalance;

import org.plantre.common.utils.CollectionUtil;

import java.util.List;
import java.util.Random;

public class RandomLoadBalance implements LoadBalance {

    public  String doSelect(List<String> serviceAddresses, String rpcServiceName){
        Random random = new Random();
        return serviceAddresses.get(random.nextInt(serviceAddresses.size()));
    }

    @Override
    public String selectServiceAddress(List<String> serviceAddresses, String rpcServiceName) {
        if (CollectionUtil.isEmpty(serviceAddresses)) {
            return null;
        }
        if (serviceAddresses.size() == 1) {
            return serviceAddresses.get(0);
        }
        return doSelect(serviceAddresses, rpcServiceName);
    }


}

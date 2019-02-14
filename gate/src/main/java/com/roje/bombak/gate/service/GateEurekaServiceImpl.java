package com.roje.bombak.gate.service;

import com.roje.bombak.common.api.service.EurekaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.context.ApplicationListener;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/2
 **/
@Slf4j
public class GateEurekaServiceImpl implements EurekaService, ApplicationListener<HeartbeatEvent> {


    private final DiscoveryClient discoveryClient;

    private Map<Short, String> serviceIdMap = new ConcurrentHashMap<>();

    public GateEurekaServiceImpl(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @Override
    public void start() {
        refreshServiceInstances();
    }


    private void refreshServiceInstances() {
        List<String> serviceNames = discoveryClient.getServices();
        Map<Short, String> idMap = new ConcurrentHashMap<>(15);
        for (String name : serviceNames) {
            List<ServiceInstance> serviceInstances = discoveryClient.getInstances(name);
            ServiceInstance instance = serviceInstances.get(0);
            short type = Short.parseShort(instance.getMetadata().get("type"));
            idMap.put(type, name);
        }
        this.serviceIdMap = idMap;
    }

    @Override
    public void stop() {}


    @SuppressWarnings("NullableProblems")
    @Override
    public void onApplicationEvent(HeartbeatEvent event) {
        refreshServiceInstances();
    }

    @Override
    public String getServiceId(short serviceType) {
        return serviceIdMap.get(serviceType);
    }
}

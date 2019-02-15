package com.roje.bombak.common.eureka;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * @author pc
 * @version 1.0
 * @date 2018/12/28
 **/
@Getter@Setter
public class ServiceInfo {

    private String serviceType;

    private String serviceId;

    private String ip;

    private int port;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServiceInfo info = (ServiceInfo) o;
        return serviceType.equalsIgnoreCase(info.getServiceType()) &&
                serviceId.equalsIgnoreCase(info.getServiceId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceType, serviceId);
    }

    @Override
    public String toString() {
        return "ServiceInfo{" +
                "serviceType='" + serviceType + '\'' +
                ", serviceId='" + serviceId + '\'' +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                '}';
    }
}

package com.roje.bombak.common.service;

import org.springframework.stereotype.Service;

/**
 * @author pc
 * @version 1.0
 * @date 2019/1/2
 **/
@Service
public interface EurekaService extends ServerService {

    /**
     * 根据服务类型获取该服务的一个服务器id
     *
     * @param serviceType 服务类型
     * @return 服务id
     */
    String getServiceId(short serviceType);
}

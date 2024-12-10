package com.mcinfotech.event.transmit.http;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.net.URI;
import java.util.Map;

/**
 * date 2022/10/24 17:37
 * @version V1.0
 * @Package com.mcinfotech.event.transmit.http
 */
@FeignClient(value = "open-feign-service",url = "EMPTY")
public interface OpenFeignService {
    @PostMapping
    String getRequest(URI uri, @RequestBody Map message);

}

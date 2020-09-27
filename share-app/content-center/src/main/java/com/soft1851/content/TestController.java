package com.soft1851.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Random;

/**
 * @author crq
 */
@Slf4j
@RestController
@RequestMapping(value = "/test")
public class TestController {

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/discovery")
    public List<ServiceInstance> getInstances(){
        //查询指定服务的所有实例信息
        return this.discoveryClient.getInstances("user-center");
    }

    @GetMapping(value = "/call/hello")
    public String callUserCenter(){
        List<ServiceInstance> instances = discoveryClient.getInstances("user-center");
        ServiceInstance serviceInstance = instances.get(new Random().nextInt(instances.size()));
        String targetUrl = serviceInstance.getUri() + "/user/hello";
//        String targetUrl = instances.stream()
//                .map(instance -> instance.getUri().toString()+"/user/hello")
//                .findFirst()
//                .orElseThrow(() -> new IllegalArgumentException("当前没有实例"));
        log.info("请求的目标地址：{}",targetUrl);
        return restTemplate.getForObject(targetUrl,String.class);
    }

    @GetMapping(value = "/call/ribbon")
    public String callByRibbon() {
        return  restTemplate.getForObject("http://user-center/user/hello",String.class);
    }

}

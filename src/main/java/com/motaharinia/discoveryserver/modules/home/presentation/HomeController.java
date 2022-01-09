package com.motaharinia.discoveryserver.modules.home.presentation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author eng.motahari@gmail.com<br>
 * کلاس کنترلر خانه
 */
@Slf4j
@RestController
public class HomeController {

    private final DiscoveryClient discoveryClient;


    @Value("${spring.application.name}")
    private String springApplicationName;

    public HomeController(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @GetMapping("/")
    public String readHome() {
        StringBuilder stringBuilder=new StringBuilder("=========="+springApplicationName+"==========<br>");
        if (this.discoveryClient == null) {
            log.info("Discovery client is null");
        } else {
            log.info("Discovery client is not null");
            List<String> serviceIdList = this.discoveryClient.getServices();
            for (String serviceId : serviceIdList) {
                List<ServiceInstance> serviceInstanceList = this.discoveryClient.getInstances(serviceId);
                for (ServiceInstance serviceInstance : serviceInstanceList) {
                    stringBuilder.append("<hr><br>serviceId:"+ serviceId + " <br>serviceInstance.getInstanceId():" + serviceInstance.getInstanceId() +" <br>host-port: http://" + serviceInstance.getHost() + ":" + serviceInstance.getPort());
                }
            }
        }
        return stringBuilder.toString();
    }



}

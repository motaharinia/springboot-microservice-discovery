package com.motaharinia.discoveryserver.config.gateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfiguration {
    @Bean
    public GatewayRequestFilter gatewayRequestFilter() {
        return new GatewayRequestFilter();
    }
//    @Bean
//    public GatewayResponseFilter gatewayResponseFilter(){
//        return new GatewayResponseFilter();
//    }
}

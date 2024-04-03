package com.gateway;

import com.user.feign.api.UserClient;
import com.user.feign.api.UserReactiveClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import reactivefeign.spring.config.EnableReactiveFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableReactiveFeignClients(clients = UserReactiveClient.class)
//@EnableFeignClients(clients = UserClient.class) 调式同步调用的时候可以将注释打开
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}

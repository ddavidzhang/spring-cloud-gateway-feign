package com.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.user.feign.api.UserReactiveClient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class ApiAuthGatewayFilterFactory extends AbstractGatewayFilterFactory<ApiAuthGatewayFilterFactory.Config> {

    private static final String USER_HEADER_NAME = "User-Info";

    @Autowired
    private UserReactiveClient userReactiveClient;


    public ApiAuthGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Collections.singletonList("checkAuth");
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (config.checkAuth) {
                String cookie = exchange.getRequest().getHeaders().getFirst("Cookie");
                String url = exchange.getRequest().getPath().toString();
                String httpMethod = exchange.getRequest().getMethodValue();
                return userReactiveClient.checkPermission(url, httpMethod, cookie).flatMap(commonResponse -> {
                    if (!commonResponse.isSuccess()) {
                        return Mono.defer(() -> {
                            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                            final ServerHttpResponse response = exchange.getResponse();
                            byte[] bytes = JSON.toJSONString(commonResponse).getBytes(StandardCharsets.UTF_8);
                            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
                            return response.writeWith(Flux.just(buffer));
                        });
                    } else {
                        log.info("User-Info: [{}]", JSON.toJSONString(commonResponse.getData()));
                        ServerHttpRequest request = exchange.getRequest().mutate().header(USER_HEADER_NAME, JSON.toJSONString(commonResponse.getData())).build();
                        return chain.filter(exchange.mutate().request(request).build());
                    }
                });
            } else {
                return chain.filter(exchange);
            }
        };
    }

    @NoArgsConstructor
    @Getter
    @Setter
    @ToString
    public static class Config {
        private boolean checkAuth;
    }
}

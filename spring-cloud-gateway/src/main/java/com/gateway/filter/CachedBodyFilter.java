package com.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class CachedBodyFilter implements GlobalFilter, Ordered {

    public static final String CACHE_REQUEST_BODY_OBJECT_KEY = "cachedRequestBodyObject";

    private final List<HttpMessageReader<?>> messageReaders = HandlerStrategies.withDefaults().messageReaders();


    @SuppressWarnings("deprecation")
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (exchange.getAttribute(CACHE_REQUEST_BODY_OBJECT_KEY) != null) {
            return chain.filter(exchange);
        }
        ServerHttpRequest request = exchange.getRequest();
        HttpMethod method = request.getMethod();
        String contentType = request.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
        if (HttpMethod.POST == method || HttpMethod.PUT == method) {
            // POST或PUT请求，下面指定的类型需要缓存body数据，文件上传之类的请求无需缓存
            if (MediaType.APPLICATION_FORM_URLENCODED_VALUE.equalsIgnoreCase(contentType)
                    || MediaType.APPLICATION_JSON_VALUE.equalsIgnoreCase(contentType)
                    || MediaType.APPLICATION_JSON_UTF8_VALUE.equalsIgnoreCase(contentType)) {
                return ServerWebExchangeUtils.cacheRequestBodyAndRequest(exchange,
                        (serverHttpRequest) -> ServerRequest
                                .create(exchange.mutate().request(serverHttpRequest).build(), messageReaders)
                                .bodyToMono(String.class).doOnNext(objectValue -> exchange.getAttributes()
                                        .put(CACHE_REQUEST_BODY_OBJECT_KEY, objectValue)).then(chain.filter(exchange)));
            }
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}

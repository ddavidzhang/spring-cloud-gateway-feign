package com.gateway.filter;

import com.gateway.util.XssCleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.CachedBodyOutputMessage;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SuppressWarnings("rawtypes")
@Component
public class XssFilterGatewayFilterFactory extends AbstractGatewayFilterFactory {


    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            HttpMethod method = request.getMethod();
            String contentType = request.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
            /*
            xss过滤器目前只处理POST和PUT相关JSON请求，其他请求因为涉及到urlDecode和urlEncode，目前没有好的处理方案
             */
            if ((HttpMethod.POST == method || HttpMethod.PUT == method) &&
                    (MediaType.APPLICATION_JSON_VALUE.equalsIgnoreCase(contentType) || MediaType.APPLICATION_JSON_UTF8_VALUE.equalsIgnoreCase(contentType))) {
                String bodyString = exchange.getAttribute(CachedBodyFilter.CACHE_REQUEST_BODY_OBJECT_KEY);
                if (StringUtils.isBlank(bodyString)) {
                    return chain.filter(exchange);
                }
                bodyString = XssCleanUtils.cleanXss(bodyString);
                BodyInserter<Mono<String>, ReactiveHttpOutputMessage> bodyInserter = BodyInserters.fromPublisher(Mono.just(bodyString), String.class);
                HttpHeaders headers = new HttpHeaders();
                headers.putAll(exchange.getRequest().getHeaders());
                headers.remove(HttpHeaders.CONTENT_LENGTH);
                CachedBodyOutputMessage outputMessage = new CachedBodyOutputMessage(exchange, headers);
                return bodyInserter.insert(outputMessage, new BodyInserterContext())
                        .then(Mono.defer(() -> {
                            ServerHttpRequest decorator = new ServerHttpRequestDecorator(exchange.getRequest()) {
                                @SuppressWarnings("NullableProblems")
                                @Override
                                public HttpHeaders getHeaders() {
                                    long contentLength = headers.getContentLength();
                                    HttpHeaders httpHeaders = new HttpHeaders();
                                    httpHeaders.putAll(headers);
                                    if (contentLength > 0) {
                                        httpHeaders.setContentLength(contentLength);
                                    } else {
                                        httpHeaders.set(HttpHeaders.TRANSFER_ENCODING, "chunked");
                                    }
                                    return httpHeaders;
                                }

                                @SuppressWarnings("NullableProblems")
                                @Override
                                public Flux<DataBuffer> getBody() {
                                    return outputMessage.getBody();
                                }
                            };
                            return chain.filter(exchange.mutate().request(decorator).build());
                        }));
            }
            return chain.filter(exchange);
        };
    }
}

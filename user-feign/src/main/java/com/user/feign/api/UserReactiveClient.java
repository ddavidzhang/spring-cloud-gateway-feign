package com.user.feign.api;

import com.user.feign.response.ResultResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactivefeign.spring.config.ReactiveFeignClient;
import reactor.core.publisher.Mono;

@SuppressWarnings("rawtypes")
@ReactiveFeignClient(name = "user-feign")
public interface UserReactiveClient {

    // 这里的Cookie通过header请求头设置，透传给用户鉴权模块
    @GetMapping(value = "/feign/api/v1/users/checkPermission", headers = "Cookie={cookie}")
    Mono<ResultResponse> checkPermission(@RequestParam String url, @RequestParam String httpMethod, @RequestParam String cookie);

}

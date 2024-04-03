package com.user.feign.api;

import com.user.feign.response.ResultResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@SuppressWarnings("rawtypes")
@FeignClient(name = "user-feign")
public interface UserClient {

    // 这里的Cookie通过header请求头设置，透传给用户鉴权模块
    @GetMapping(value = "/feign/api/v1/users/checkPermission", headers = "Cookie={cookie}")
    ResultResponse checkPermission(@RequestParam String url, @RequestParam String httpMethod, @RequestParam String cookie);

}

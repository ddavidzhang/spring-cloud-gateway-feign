package com.user.feign.util;

import com.user.feign.response.ResultResponse;

public class ResponseUtil {

    public static <T> ResultResponse<T> build(T data) {
        ResultResponse<T> resultResponse = new ResultResponse<>();
        resultResponse.setData(data);
        return resultResponse;
    }

    public static <T> ResultResponse<T> success() {
        return build(null);
    }

}

package com.user.feign.response;

import lombok.Data;

@Data
public class ResultResponse<T> {
    private boolean success = true;
    private String errorCode;
    private String errorMsg;
    private T data;
}

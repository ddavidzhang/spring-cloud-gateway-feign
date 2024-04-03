package com.user.feign.domain;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserDTO {

    private String username;

    private String userRole;

    private String userMail;

    private String userPhone;

    private LocalDateTime createTime;
    private LocalDateTime modifiedTime;

}

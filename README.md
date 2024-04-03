# Spring Cloud Gateway ReactiveFeign

作为 demo 工程，演示了如何在 Spring Cloud Gateway 中使用 ReactiveFeign 进行服务调用。

## 说明
本项目包含user-feign、spring-cloud-gateway两个子模块。

## 项目启动步骤
1、启动nacos，因为feign调用会依赖注册中心，所以需要启动nacos
2、启动user-feign项目
3、启动spring-cloud-gateway项目

## 测试
用postman发起接口调用：

`GET localhost:6666/api/v1/users/getUserInfo`

成功返回用户信息则说明调用成功

## 其他
其他关注的地方，可以通过断点进行调试。

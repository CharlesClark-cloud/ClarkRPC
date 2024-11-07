package com.clarkrpc.annotation;

import java.lang.annotation.*;

/**
 * RPC service annotation, marked on the service implementation class
 * 此注解 用来标记接口服务提供类
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited //该注解表明 修饰的类的子类可以继承该注解的
public @interface RpcService {

    /**
     * Service version, default value is empty str
     * 服务版本号 用于相同服务区分不同版本
     */
    String version() default "";

    /**
     * Service group, default value is empty string
     * 服务分组 用于相同服务区分不同版本
     */
    String group() default "";

}

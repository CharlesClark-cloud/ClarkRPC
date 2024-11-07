package com.clarkrpc.annotation;


import java.lang.annotation.*;

/**
 * RPC reference annotation, autowire the service implementation class
 * RPC引用注释，自动连接服务实现类
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface RpcReference {

    /**
     * Service version, default value is empty string
     */
    String version() default "";

    /**
     * Service group, default value is empty string
     */
    String group() default "";

}

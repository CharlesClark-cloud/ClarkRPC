package com.clarkrpc.annotation;


import com.clarkrpc.spring.CustomScannerRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 扫描自定义注解
 *
 */
@Target({ElementType.TYPE, ElementType.METHOD}) //定义注解使用目标
@Retention(RetentionPolicy.RUNTIME) //定义注解运行范围
@Import(CustomScannerRegistrar.class)  //import 注解会将该类注册为spring bean，以后就可以直接拿来使用了
@Documented
public @interface RpcScan {

    String[] basePackage();

}

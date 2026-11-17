package com.jay.aigcoj.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于权限校验   是一个自定义注解  通常用在方法上，表示只有特定角色的用户才能调用该方法
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthCheck {

    String mustRole() default "";

}


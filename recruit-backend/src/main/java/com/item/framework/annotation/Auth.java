package com.item.framework.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.item.framework.constant.RoleType;

import org.springframework.stereotype.Component;

/**
 * <p>
 * 权限认证注解
 * </p>
 *
 * @author liuyabin on 2025/7/11
 * @since 1.0.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Auth {

    /**
     * 角色类型，用于权限验证
     */
    RoleType roleType() default RoleType.NONE;
}

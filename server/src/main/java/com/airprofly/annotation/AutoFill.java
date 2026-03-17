package com.airprofly.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.springframework.aot.hint.annotation.Reflective;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.RetentionPolicy;

import com.airprofly.enumeration.OperationType;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented // javadoc 时被这个注解标记的元素科研看到 当前这个注解
@Reflective // 让这个注解在 native 镜像中可反射访问
@Inherited // 允许子类继承父类的方法上的这个注解
public @interface AutoFill {
    OperationType value() default OperationType.INSERT;
}

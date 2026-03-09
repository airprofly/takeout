package com.airprofly.aspect;

import java.lang.reflect.Method;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import com.airprofly.annotation.AutoFill;
import com.airprofly.constant.AutoFillMethodConstant;
import com.airprofly.context.BaseContext;
import com.airprofly.enumeration.OperationType;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    
    @Pointcut("execution(* com.airprofly.mapper.*.*(..))&&@annotation(com.airprofly.annotation.AutoFill)")
    public void autoFillPointcut() {}

    @Before("autoFillPointcut()")
    public void beforeAutoFill(JoinPoint joinPoint) {
        log.info("开始自动填充字段...");

        // 获取操作类型
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        AutoFill autoFill = method.getAnnotation(AutoFill.class);

        OperationType operationType = autoFill.value();
        log.info("操作类型: {}", operationType);

        Object[] args = joinPoint.getArgs();
        if(args==null || args.length == 0) {
            log.warn("没有找到方法参数，无法进行自动填充");
            return;
        }

        Object entity = args[0];
        log.info("待处理的实体对象: {}", entity);

        Long currentUserId = BaseContext.getCurrentId();

        
        if(operationType == OperationType.INSERT) {

            try {
                Method entitySetCreateTimeMethod = entity.getClass().getMethod(AutoFillMethodConstant.SET_CREATE_TIME_METHOD_NAME, java.time.LocalDateTime.class);
                Method entitySetUpdateTimeMethod = entity.getClass().getMethod(AutoFillMethodConstant.SET_UPDATE_TIME_METHOD_NAME, java.time.LocalDateTime.class);
                Method entitySetCreateUserMethod = entity.getClass().getMethod(AutoFillMethodConstant.SET_CREATE_USER_METHOD_NAME, Long.class);
                Method entitySetUpdateUserMethod = entity.getClass().getMethod(AutoFillMethodConstant.SET_UPDATE_USER_METHOD_NAME, Long.class);

                entitySetCreateTimeMethod.invoke(entity, java.time.LocalDateTime.now());
                entitySetUpdateTimeMethod.invoke(entity, java.time.LocalDateTime.now());
                entitySetCreateUserMethod.invoke(entity, currentUserId);
                entitySetUpdateUserMethod.invoke(entity, currentUserId);

            } catch (Exception e) {
                log.error("自动填充字段失败", e);
                e.printStackTrace();
            }
            
        } else if(operationType == OperationType.UPDATE) {
            try {
                Method entitySetUpdateTimeMethod = entity.getClass().getMethod(AutoFillMethodConstant.SET_UPDATE_TIME_METHOD_NAME, java.time.LocalDateTime.class);
                Method entitySetUpdateUserMethod = entity.getClass().getMethod(AutoFillMethodConstant.SET_UPDATE_USER_METHOD_NAME, Long.class);

                entitySetUpdateTimeMethod.invoke(entity, java.time.LocalDateTime.now());
                entitySetUpdateUserMethod.invoke(entity, currentUserId);

            } catch (Exception e) {
                log.error("自动填充字段失败", e);
                e.printStackTrace();
            }
        }else{
            log.warn("不支持的操作类型: {}", operationType);
        }

    }

}

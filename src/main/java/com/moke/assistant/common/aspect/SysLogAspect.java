package com.moke.assistant.common.aspect;

import com.moke.assistant.common.annotation.SysLog;
import com.moke.assistant.common.utils.HttpContextUtils;
import com.moke.assistant.common.utils.IPUtils;
import com.moke.assistant.common.utils.JwtUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Date;

/**
 * 系统日志切面
 */
@Aspect
@Component
public class SysLogAspect {
    private static final Logger logger = LoggerFactory.getLogger(SysLogAspect.class);

    @Autowired
    private JwtUtils jwtUtils;

    private ThreadLocal<Long> startTime = new ThreadLocal<>();
    private ThreadLocal<String> requestPath = new ThreadLocal<>();
    private ThreadLocal<String> methodName = new ThreadLocal<>();
    private ThreadLocal<String> operation = new ThreadLocal<>();
    private ThreadLocal<Long> userId = new ThreadLocal<>();
    private ThreadLocal<String> username = new ThreadLocal<>();
    private ThreadLocal<String> ip = new ThreadLocal<>();

    /**
     * 定义切点
     */
    @Pointcut("@annotation(com.moke.assistant.common.annotation.SysLog)")
    public void logPointCut() {
    }

    /**
     * 前置通知
     */
    @Before("logPointCut()")
    public void before(JoinPoint joinPoint) {
        startTime.set(System.currentTimeMillis());

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            requestPath.set(request.getRequestURL().toString());
            ip.set(IPUtils.getIpAddr(request));

            // 从请求头中获取token并解析用户信息
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                Long userIdVal = jwtUtils.getUserIdFromToken(token);
                String usernameVal = jwtUtils.getUsernameFromToken(token);
                userId.set(userIdVal);
                username.set(usernameVal);
            }
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        methodName.set(method.getName());

        SysLog sysLog = method.getAnnotation(SysLog.class);
        if (sysLog != null) {
            operation.set(sysLog.value());
        }

        logger.info("开始执行: {}", operation.get());
        logger.info("请求路径: {}", requestPath.get());
        logger.info("请求IP: {}", ip.get());
        logger.info("用户ID: {}", userId.get());
        logger.info("用户名: {}", username.get());
    }

    /**
     * 后置通知
     */
    @AfterReturning(returning = "result", pointcut = "logPointCut()")
    public void afterReturning(Object result) {
        long time = System.currentTimeMillis() - startTime.get();

        logger.info("执行完成: {}", operation.get());
        logger.info("执行方法: {}", methodName.get());
        logger.info("执行耗时: {}ms", time);
        logger.info("返回结果: {}", result);

        // 清理ThreadLocal
        startTime.remove();
        requestPath.remove();
        methodName.remove();
        operation.remove();
        userId.remove();
        username.remove();
        ip.remove();
    }
}
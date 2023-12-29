package com.ruoyi.framework.aspectj;

import com.ruoyi.common.core.domain.BaseEntity;
import com.ruoyi.common.utils.SecurityUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;


/**
 * @author xiao.hu
 * @date 2023-12-23
 * @apiNote
 */
@Aspect
@Component
public class SaveActionAspect {

    @Pointcut("execution(public * com.ruoyi.*..*Impl.insert*(..))")
    public void beforeSave() {

    }

    @Before("beforeSave()")
    public void doBefore(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if(args.length == 0) {
            return;
        }

        BaseEntity baseEntity = (BaseEntity) args[0];

        String username = SecurityUtils.getUsername();
        baseEntity.setCreateBy(username);
        baseEntity.setUpdateBy(username);
    }
}

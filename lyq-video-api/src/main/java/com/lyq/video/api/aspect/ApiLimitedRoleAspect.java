package com.lyq.video.api.aspect;

import com.lyq.video.api.support.UserSupport;
import com.lyq.video.domain.annotation.ApiLimitedRole;
import com.lyq.video.domain.auth.UserRole;
import com.lyq.video.domain.exception.ConditionException;
import com.lyq.video.service.UserRoleService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Order(1)
@Component
@Aspect
public class ApiLimitedRoleAspect {

    @Autowired
    private UserSupport userSupport;

    @Autowired
    private UserRoleService userRoleService;

    @Pointcut("@annotation(com.lyq.video.domain.annotation.ApiLimitedRole)")
    public void check(){
    }

    @Before("check() && @annotation(apiLimitedRole)")
    public void doBefore(JoinPoint joinPoint, ApiLimitedRole apiLimitedRole){
        Long userId = userSupport.getCurrentUserId();
        List<UserRole> userRoleList = userRoleService.getUserRoleByUserId(userId);
        String[] limitedRoleCodeList = apiLimitedRole.limitedRoleCodeList();
        Set<String> limitedRoleCodeSet = Arrays.stream(limitedRoleCodeList).collect(Collectors.toSet());
        Set<String> roleCodeSet = userRoleList.stream().map(UserRole::getRoleCode).collect(Collectors.toSet());
        roleCodeSet.retainAll(limitedRoleCodeSet);
        if(roleCodeSet.size() > 0){
            throw new ConditionException("权限不足！");
        }
    }
}

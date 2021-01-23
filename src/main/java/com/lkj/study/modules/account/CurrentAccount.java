package com.lkj.study.modules.account;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * AuthenticationPrincipal
 * - 핸들러 매개 변수로 현재 인증된 Principal 참조 가능
 */
@Retention(RetentionPolicy.RUNTIME) //runtime 까지
@Target(ElementType.PARAMETER)  //parameter 에서만 사용
@AuthenticationPrincipal(expression = "#this == 'anonymousUser' ? null : account")    //인증되지 않은 사용자 -> null
public @interface CurrentAccount {
}

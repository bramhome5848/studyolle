package com.lkj.study.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity  //security 설정을 직접 하게 됨
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //특정 요청들을 security check 하지 않도록 설정
        http.authorizeRequests()
                .mvcMatchers("/", "/login", "sign-up", "check-email-token",
                        "/email-login", "/check-email-login", "/login-link").permitAll()
                .mvcMatchers(HttpMethod.GET, "/profile/*").permitAll()
                .anyRequest().authenticated();  //나머지는 security check

        //form login 기능 사용
        http.formLogin()
                .loginPage("/login").permitAll();

        //logout
        http.logout()
                .logoutSuccessUrl("/"); //logout 했을 때 어디로 갈지
    }

    @Override
    public void configure(WebSecurity web) {
        //static resource 는 인증하지 말도록 설정
        web.ignoring()
                .mvcMatchers("/node_modules/**")
                .antMatchers("/favicon.ico", "/resources/**", "/error") //login 할 때 정적 컨텐츠가 없어 나는 error 인해 추가(ex 이미지)
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }
}

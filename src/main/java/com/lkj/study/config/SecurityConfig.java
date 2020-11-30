package com.lkj.study.config;

import com.lkj.study.account.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity  //security 설정을 직접 하게 됨
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final AccountService accountService;
    private final DataSource dataSource;    //jpa 사용하고 있기 때문에 bean 등록 되어 있음

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

        /**
         * 1. spring security 해시 기반의 쿠키
         * username, password, 만료기간, key(애플리케이션 마다 다른 값)
         * 치명적인 단점 - 쿠키를 다른 사람이 가져가면 그 계정은 탈취당한 것 -> 쿠키를 통해 비밀번호 변경해버림...
         *
         * 2. 개선한 방법
         * username, 토큰(랜덤, 매번바뀜), 시리즈(랜덤, 고정)
         * 쿠키를 탈취 당한 경우, 희생자는 유효하지 않은 토큰과 유효한 시리즈와 username 으로 접속 에
         * -> 탈취 당한 경우 이미 새로운 토큰으 갱신되었기 때문 기존의 토큰은 유효하지 않음
         * 이 경우, 모든 토큰을 삭제하여 해커가 더이상 탈취한 쿠키를 사용하지 못하도록 방지 -> login 을 다시 하도록 유도
         */
        //remember me
        //1. 해시 기반의 쿠키
//        http.rememberMe()
//                .key("sdfsdfsdf");

        //2. 개선 방법
        http.rememberMe()
                .userDetailsService(accountService)  //userDetail -> accountService 에서 구현했기 때문에 사용가능
                .tokenRepository(tokenRepository());
    }

    @Bean
    public PersistentTokenRepository tokenRepository() {
        //jdbc 기반의 토큰 repository 구현체
        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
        jdbcTokenRepository.setDataSource(dataSource);
        return jdbcTokenRepository;
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

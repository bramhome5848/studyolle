package com.lkj.study.account;

import com.lkj.study.config.AppProperties;
import com.lkj.study.domain.Account;
import com.lkj.study.domain.Tag;
import com.lkj.study.domain.Zone;
import com.lkj.study.mail.EmailMessage;
import com.lkj.study.mail.EmailService;
import com.lkj.study.settings.form.Profile;
import com.lkj.study.settings.form.Notifications;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountService implements UserDetailsService { //spring security class 상속

    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;

    public Account processNewAccount(SignUpForm signUpForm) {
        Account newAccount = saveNewAccount(signUpForm);
        sendSignUpConfirmEmail(newAccount);
        return newAccount;
    }

    private Account saveNewAccount(@Valid SignUpForm signUpForm) {
        /**
         * encoding -> plaintext + salt hashing -> db 저장
         * login -> 사용자 입력 plaintext, db 저장된 비민번호를 함께 해싱 -> 같은지 다른지 판별
         */

        signUpForm.setPassword(passwordEncoder.encode(signUpForm.getPassword()));
        Account account = modelMapper.map(signUpForm, Account.class);
        account.generateEmailCheckToken();
        return accountRepository.save(account);
    }

    public void sendSignUpConfirmEmail(Account newAccount) {
        Context context = new Context();
        context.setVariable("link", "/check-email-token?token=" + newAccount.getEmailCheckToken() +
                "&email=" + newAccount.getEmail());
        context.setVariable("nickname", newAccount.getNickname());
        context.setVariable("linkName", "이메일 인증하기");
        context.setVariable("message", "스터디올래 서비스를 사용하려면 링크를 클릭하세요.");
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(newAccount.getEmail())
                .subject("스터디올래, 회원 가입 인증")
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }

    public void login(Account account) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                new UserAccount(account),  //principal
                account.getPassword(),  //credentials
                List.of(new SimpleGrantedAuthority("ROLE_USER")));  //role list
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    /**
     *
     * spring security 로그인 기본값
     * username, password, Post "/login"
     */
    @Override
    public UserDetails loadUserByUsername(String emailOrNickname) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(emailOrNickname);
        if (account == null) {
            account = accountRepository.findByNickname(emailOrNickname);
        }

        if (account == null) {
            throw new UsernameNotFoundException(emailOrNickname);
        }

        return new UserAccount(account);
    }

    /**
     * Open EntityManager( 또는 Session) In view 필터
     * 영속성 컨텍스트 요청을 처리하는 전체 프로세스에서 바인딩 시켜주는 필터.
     * 뷰를 랜더링 할 때 까지 영속성 컨텍스트를 유지하기 때문에 필요한 데이터를 랜더링 하는 시점에 추가로 읽어올 수 있음(DB 커넥션은 계속 유지하고 있기 때문에)
     * 엔티티 객체 변경은 반드시 트랜잭션 안에서 할 것 -> 트랙잭션 종료 직전 또는 필요한 시점에 변경 사항을 DB 반영
     * Controller -> Service -> Repository 이용일 때 트랜잭션은 Service, Repository 적용
     * Controller -> Repository 용일 때 트랜재션은 Repository 적용
     *
     * 본 프로젝트에서는 데이터 변경은 서비스 계층으로 위임해서 트랜잭션 안에서 처리
     */

    public void completeSingUp(Account account) {
        account.completeSingUp();
        login(account);
    }

    /**
     * OSIV 자료
     * OSIV 전략은 트랜잭션 시작처럼 최초 데이터베이스 커넥션 시작 시점부터 API 응답이 끝날 때 까지 영속성 컨텍스트와 데이터베이스 커넥션을 유지.
     * 그래서 View Template이나 API 컨트롤러에서 지연 로딩이 가능
     * 지연 로딩은 영속성 컨텍스트가 살아있어야 가능하고, 영속성 컨텍스트는 기본적으로 데이터베이스 커넥션을 유지.
     * 이 전략은 너무 오랜시간동안 데이터베이스 커넥션 리소스를 사용, 실시간 트래픽이 중 요한 애플리케이션에서는 커넥션이 모자랄 수 있음 -> 장애로 이어질 수 있음
     * ex) 컨트롤러에서 외부 API를 호출하면 외부 API 대기 시간 만큼 커넥션 리소스를 반환하지 못하고 유지
     *
     * OSIV를 끄면 트랜잭션을 종료할 때 영속성 컨텍스트를 닫고, 데이터베이스 커넥션도 반환
     * OSIV를 끄면 모든 지연로딩을 트랜잭션 안에서 처리
     * 지연 로딩 코드를 트랜잭션 안으로 넣어야 함. 그리고 view template에서 지연로딩이 동작하지 않음.
     * 결론적으로 트랜잭션이 끝나기 전에 지연 로딩을 강제로 호출
     */


    /**
     * account -> detached 상태
     * detached -> 영속성 컨텍스트에 저장되었다가 분리된 상태
     * transient(new) -> 양속성 컨텍스트와 관계가 없는 상태 -> 대체로 auto inc 로 인해 id 가 존재
     * managed -> 영속성 컨텍스에 저장된 상태
     */
    public void updateProfile(Account account, Profile profile) {
        modelMapper.map(profile, account); //source -> destination
        accountRepository.save(account);    //detached 상태 객체를 merge 하게 됨
    }

    public void updatePassword(Account account, String newPassword) {
        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }

    public void updateNotifications(Account account, Notifications notifications) {
        modelMapper.map(notifications, account);    //source -> destination
        accountRepository.save(account);
    }

    public void updateNickname(Account account, String nickname) {
        account.setNickname(nickname);
        accountRepository.save(account);
        login(account);
    }

    public void sendLoginLink(Account account) {
        Context context = new Context();
        context.setVariable("link", "/login-by-email?token=" + account.getEmailCheckToken() +
                "&email=" + account.getEmail());
        context.setVariable("nickname", account.getNickname());
        context.setVariable("linkName", "스터디올래 로그인하기");
        context.setVariable("message", "로그인 하려면 아래 링크를 클릭하세요.");
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(account.getEmail())
                .subject("스터디올래, 로그인 링크")
                .message(message)
                .build();
        emailService.sendEmail(emailMessage);
    }

    public void addTag(Account account, Tag tag) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getTags().add(tag));
        //findById -> eager loading
        //getOne -> lazy loading
    }

    public Set<Tag> getTags(Account account) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        return byId.orElseThrow().getTags();
    }

    public void removeTag(Account account, Tag tag) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getTags().remove(tag));
    }

    public Set<Zone> getZones(Account account) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        return byId.orElseThrow().getZones();
    }

    public void addZone(Account account, Zone zone) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getZones().add(zone));
    }

    public void removeZone(Account account, Zone zone) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getZones().remove(zone));
    }
}

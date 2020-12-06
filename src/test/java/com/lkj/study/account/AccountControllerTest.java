package com.lkj.study.account;

import com.lkj.study.domain.Account;
import com.lkj.study.mail.EmailMessage;
import com.lkj.study.mail.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @MockBean
    EmailService emailService;

    @DisplayName("인증 메일 확인 - 입력값 오류 | 보이는지 테스트")
    @Test
    void checkEmailToken_with_wrong_input() throws Exception {
        mockMvc.perform(get("/check-email-token")
                .param("token", "sdfjsdflksdjf")
                .param("email", "email@gmail.com"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(view().name("account/checked-email"))
                .andExpect(unauthenticated()); //인증 되지 않았는지 확인
    }

    @DisplayName("인증 메일 확인 - 입력값 정상")
    @Test
    void checkEmailToken() throws Exception {

        Account account = Account.builder()
                .email("test@gmail.com")
                .password("1234567")
                .nickname("lkj")
                .build();

        Account newAccount = accountRepository.save(account);
        newAccount.generateEmailCheckToken();

        mockMvc.perform(get("/check-email-token")
                .param("token", newAccount.getEmailCheckToken())
                .param("email", newAccount.getEmail()))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(model().attributeExists("nickname"))
                .andExpect(model().attributeExists("numberOfUser"))
                .andExpect(view().name("account/checked-email"))
                .andExpect(authenticated().withUsername("lkj")); //인증 되었는지 확인
    }

    @DisplayName("회원 가입 화면 보이는지 확인 테스트")
    @Test
    void signUpForm() throws Exception {
        //SecurityConfig 에서 sign-up을 제외시키지 않으면 테스트는 실패하게 됨
        mockMvc.perform(get("/sign-up"))
                .andDo(print()) //view code 확인
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(model().attributeExists("signUpForm"))
                .andExpect(unauthenticated());    //인증이 되었는지

    }

    @DisplayName("회원 가입 처리 -  입력값 오류")
    @Test
    void singUpSubmit_with_wrong_input() throws Exception {
        /**
         * csrf token -> 내가 만들어준 폼에서 전송해준 데이터인지 확인
         * srf token 이 없고 데이터만 올 경우 혹은 token 값이 다른 경우 -> 403 error, 안전한 데이터가 아니라고 판단
         * sign-up data 를 security 에서 허용했지만 유효하지 않다가 판단함
         * 인증하지 않고 사용해도 된다고 허용했지만 안전하지 않은 요청에 대해서까지 받아들이지는 않음
         */
        mockMvc.perform(post("/sign-up")
                .param("nickname", "lkj")
                .param("email","email")
                .param("password", "12345")
                .with(csrf()))  //with csrf token
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(unauthenticated());  //인증이 되지 않았는지
    }

    @DisplayName("회원 가입 처리 -  입력값 정상")
    @Test
    void singUpSubmit_with_correct_input() throws Exception {
        mockMvc.perform(post("/sign-up")
                .param("nickname", "lkj")
                .param("email","bramhome5848@gmail.com")
                .param("password", "12345678")
                .with(csrf()))  //with csrf token
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"))
                .andExpect(authenticated().withUsername("lkj"));    //인증이 되었는지

        Account account = accountRepository.findByEmail("bramhome5848@gmail.com");
        assertNotNull(account);
        assertNotEquals(account.getPassword(), "12345678");
        assertTrue(accountRepository.existsByEmail("bramhome5848@gmail.com"));
        assertNotNull(account.getEmail());

        //메일을 보내는지 확인(send 호출되었는지 확인)
        then(emailService).should().sendEmail(any(EmailMessage.class));
    }
}
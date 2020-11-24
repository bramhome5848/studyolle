package com.lkj.study.account;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @MockBean
    JavaMailSender javaMailSender;

    @DisplayName("회원 가입 화면 보이는지 확인 테스트")
    @Test
    void signUpForm() throws Exception {
        //SecurityConfig 에서 sign-up을 제외시키지 않으면 테스트는 실패하게 됨
        mockMvc.perform(get("/sign-up"))
                .andDo(print()) //view code 확인
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(model().attributeExists("signUpForm"));
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
                .andExpect(view().name("account/sign-up"));
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
                .andExpect(view().name("redirect:/"));

        assertTrue(accountRepository.existsByEmail("bramhome5848@gmail.com"));

        //메일을 보내는지 확인(send 호출되었는지 확인)
        then(javaMailSender).should().send(any(SimpleMailMessage.class));
    }
}
package com.lkj.study.account;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final SignUpFormValidator signUpFormValidator;
    private final AccountService accountService;

    @InitBinder("signUpForm")   //signUpForm data를 받을 때 사용할 binder 를 설정 가능
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(signUpFormValidator);
    }

    @GetMapping("/sign-up")
    public String singUpForm(Model model) {
        model.addAttribute(/*"signUpForm",*/ new SignUpForm()); //camel case 이름이 같으면 "signUpForm" 생략 가능
        return "account/sign-up";
    }

    @PostMapping("/sign-up")
    public String signUpSubmit(@Valid /* @ModelAttribute */ SignUpForm signUpForm, Errors errors) {  //@ModelAttribute 생략 가능
        if(errors.hasErrors()) {    //valid error 에 걸릴 경우
            return "account/sign-up";
        }

        accountService.processNewAccount(signUpForm);
        return "redirect:/";
    }
}

package com.lkj.study.modules.account.validator;

import com.lkj.study.modules.account.AccountRepository;
import com.lkj.study.modules.account.Account;
import com.lkj.study.modules.account.form.NicknameForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class NicknameValidator implements Validator {

    private final AccountRepository accountRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return NicknameForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        NicknameForm nickNameForm = (NicknameForm) target;
        Account byNickname = accountRepository.findByNickname(nickNameForm.getNickname());

        if(byNickname != null) {
            errors.rejectValue("nickname", "wrong.value", "입력하신 닉네임을 사용할 수 없습니다");
        }
    }
}

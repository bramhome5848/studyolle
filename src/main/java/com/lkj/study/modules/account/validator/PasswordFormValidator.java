package com.lkj.study.modules.account.validator;

import com.lkj.study.modules.account.form.PasswordForm;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class PasswordFormValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {   //어떤 타입의 form 객체를 검정 할 것인지
        return PasswordForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        PasswordForm passwordForm = (PasswordForm)target;
        if (!passwordForm.getNewPassword().equals(passwordForm.getNewPasswordConfirm())) {  //입력과 확인이 같은지 확인
            errors.rejectValue("newPassword", "wrong.value", "입력한 새 패스워드가 일치하지 않습니다.");
        }
    }
}

package com.lkj.study.main;

import com.lkj.study.account.CurrentUser;
import com.lkj.study.domain.Account;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String home(@CurrentUser Account account, Model model) {
        if(account != null) {   //인증을 한 사용자
            model.addAttribute(account);
        }

        return "index";
    }
}

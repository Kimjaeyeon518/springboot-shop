package com.shop.springboot.controller;

import com.shop.springboot.dto.userDto.UserRequestDto;
import com.shop.springboot.entity.Cart;
import com.shop.springboot.entity.Product;
import com.shop.springboot.entity.User;
import com.shop.springboot.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
public class UserController {

    private final UserService userService;

    @GetMapping("/login")
    public String goLoginPage(HttpServletRequest request) {

        String referrer = request.getHeader("Referer");
        request.getSession().setAttribute("prevPage", referrer);
        return "member/login";
    }

    @GetMapping("/register")
    public String goRegistrationPage(HttpServletRequest request) {
        return "member/register";
    }

    // 일반유저 회원가입
    @PostMapping("/register")
    public String registration(@ModelAttribute @Valid UserRequestDto userRequestDto, RedirectAttributes rttr) {
        userService.userRegistration(userRequestDto);

        rttr.addFlashAttribute("registerComplete", "회원가입이 완료되었습니다.");

        return "redirect:/login";
    }

    @GetMapping("/profiles")
    public String profiles(Model model) {

        model.addAttribute("pageName", "profiles");

        return "user/profiles";
    }

    // form 로그아웃, oauth2 로그아웃 공통
    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // remember-me 쿠키도 지워야 함
        new CookieClearingLogoutHandler(AbstractRememberMeServices.SPRING_SECURITY_REMEMBER_ME_COOKIE_KEY)
                .logout(request, response, authentication);

        new SecurityContextLogoutHandler().logout(request, response, authentication);

        return "redirect:/";
    }

    @PostMapping("/loginFailure")
    public String loginFailure() throws Exception {

        return "redirect:/login";
    }

    // 아이디 중복 로그인
    @GetMapping("/duplicated-login")
    public String duplicatedLogin(RedirectAttributes rttr) {

        rttr.addFlashAttribute("duplicatedLogin", "다른 곳에서 로그인 하였습니다.");

        return "redirect:/";
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/users/{userId}")
    public String delete(@PathVariable Long userId, RedirectAttributes rttr) {
        userService.delete(userId);
        rttr.addFlashAttribute("registerComplete", "선택 회원 삭제.");
        return "redirect:/userList";
    }
}
package org.example.blogproject.controller;

import lombok.RequiredArgsConstructor;
import org.example.blogproject.Service.RoleService;
import org.example.blogproject.Service.UserService;
import org.example.blogproject.domain.Role;
import org.example.blogproject.domain.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/minlog")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final RoleService roleService;

    @GetMapping
    public String main(){
        return "main";
    }
    @GetMapping("/login")
    public String loginForm(Model model){
        String username = null;
        String password = null;
        model.addAttribute("username", username);
        model.addAttribute("password", password);
        return "login";
    }
    @PostMapping("/login")
    public String login(@ModelAttribute String username,
                        @ModelAttribute String password,
                        RedirectAttributes redirectAttributes){
        if(userService.isExistUsername(username)){
            if(userService.getUser(username).getPassword().equals(password)){
                return "redirect:/minlog";
            }
            else{
                redirectAttributes.addAttribute("message", "Password가 옳지 않습니다.");
                redirectAttributes.addAttribute("username", username);
                return "redirect:/login";
            }
        }
        else {
            redirectAttributes.addAttribute("message", "존재하지 않는 ID입니다.");
            return "redirect:/login";
        }
    }
    @GetMapping("/userreg")
    public String userRegForm(Model model){
        model.addAttribute("user", new User());
        return "userreg";
    }
    @PostMapping("/userreg")
    public String userReg(@ModelAttribute User user){
        Role roleUser = roleService.getRole("ROLE_USER");
//        user.setRegistrationDate(LocalDateTime.now());
        user.getRoles().add(roleUser);
        roleUser.getUsers().add(user);
        //create에서 오류 발생 시 Users에 add된 user를 지워야 함
        //-> roleUser는 DB에서 select해온 객체라 DB에만 user가 반영이 안되면 상관없음
        //-> create에서 오류 발생 시 변경사항이 DB에 commit되지 않을 것
        userService.createUser(user);
        /*
        userService.createUser(user);
        roleUser.getUsers().add(user);
        //user_roles 테이블에 반영이 안 됨
        */
        return "redirect:/minlog";
    }
}

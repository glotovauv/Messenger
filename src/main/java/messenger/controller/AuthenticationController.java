package messenger.controller;

import messenger.controller.form.UserForm;
import messenger.model.RoleType;
import messenger.model.User;
import messenger.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.SessionStatus;

import java.security.Principal;

@Controller
public class AuthenticationController {

    @Autowired
    private UserService userService;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @GetMapping(value = {"/", "/index"})
    String index() {
        System.out.println(RoleType.ROLE_USER.toString());
        return "index";
    }

    @GetMapping(value = "/registration")
    String registration(Model model) {
        model.addAttribute("userForm", new UserForm());
        return "authentication/registration";
    }

    @PostMapping(value = "/registration")
    String registration(Model model, @ModelAttribute("userForm") UserForm userForm) {
        String login = userForm.getLogin();
        String firstName = userForm.getFirstName();
        String lastName = userForm.getLastName();
        String email = userForm.getEmail();
        String password = userForm.getPassword();
        String confirmPassword = userForm.getConfirmPassword();
        if (!password.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Password isn't equals!");
            return "authentication/registration";
        }
        String hashPassword = bCryptPasswordEncoder.encode(password);
        User user = new User(login, hashPassword, firstName, lastName, email);
        boolean isCreate = userService.createUser(user);
        if (!isCreate) {
            model.addAttribute("errorMessage", "Login isn't unique!");
            return "authentication/registration";
        }
        return "redirect:/index";
    }

    @GetMapping(value = "/login")
    String login(Model model) {
        model.addAttribute("userForm", new UserForm());
        return "authentication/login";
    }

    @PostMapping("/logout")
    String logout(SessionStatus status) {
        status.setComplete();
        return "redirect:/login";
    }
}

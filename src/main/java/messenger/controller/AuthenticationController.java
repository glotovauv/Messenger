package messenger.controller;

import messenger.controller.form.UserForm;
import messenger.model.User;
import messenger.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.support.SessionStatus;

import javax.validation.Valid;

@Controller
public class AuthenticationController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserService userService;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @GetMapping(value = {"/", "/index"})
    String index() {
        return "index";
    }

    @GetMapping(value = "/registration")
    String registration(Model model) {
        model.addAttribute("userForm", new UserForm());
        return "authentication/registration";
    }

    @PostMapping(value = "/registration")
    String registration(Model model,
                        @Valid @ModelAttribute("userForm") UserForm userForm,
                        BindingResult result) {
        String login = userForm.getLogin();
        String firstName = userForm.getFirstName();
        String lastName = userForm.getLastName();
        String email = userForm.getEmail();
        String password = userForm.getPassword();
        String confirmPassword = userForm.getConfirmPassword();
        if (!password.equals(confirmPassword)) {
            result.addError(new FieldError("userForm", "password", "Passwords is not equals"));
            result.addError(new FieldError("userForm", "confirmPassword", "Passwords is not equals"));
        }
        if(result.hasErrors()){
            return "authentication/registration";
        }

        String hashPassword = bCryptPasswordEncoder.encode(password);
        User user = new User(login, hashPassword, firstName, lastName, email);
        boolean isCreate = userService.createUser(user);
        if (!isCreate) {
            result.addError(new FieldError("userForm", "login", "Login is not unique"));
            return "authentication/registration";
        }
        logger.info("Create new user: id = {}, login = {}", user.getId(), user.getLogin());
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

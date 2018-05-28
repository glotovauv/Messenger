package messenger.controller;

import messenger.controller.form.EditUserForm;
import messenger.controller.form.UserForm;
import messenger.model.User;
import messenger.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class UserController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserService userService;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    private User castToUser(Principal principal) {
        return (User) ((Authentication) principal).getPrincipal();
    }

    @GetMapping("/home_page")
    String toUserPage(Principal principal) {
        User user = castToUser(principal);
        return "redirect:/user_page/" + user.getId();
    }

    @GetMapping("/user_page/{id}")
    String userPage(Model model, Principal principal, @PathVariable("id") long idUser) {
        User user = castToUser(principal);
        if (user.getId() == idUser) {
            model.addAttribute("user", user);
        } else {
            User userInfo = userService.getUserCompletely(idUser);
            model.addAttribute("user", userInfo);
            model.addAttribute("inContact", userService.isUserInContact(user, idUser));
        }
        return "user/user_page";
    }

    @GetMapping("/search")
    String searchUsers() {
        return "user/search_users";
    }

    @PostMapping("/search")
    String searchUsers(Model model, Principal principal,
                       @NotNull @RequestParam("login") String login,
                       @NotNull @RequestParam("firstName") String firstName,
                       @NotNull @RequestParam("lastName") String lastName) {
        List<User> users = userService.searchUsers(login, firstName, lastName);
        User user = castToUser(principal);
        List<String> contacts = user.getUserContacts().stream().map(User::getLogin).collect(Collectors.toList());
        model.addAttribute("searched_users", users);
        model.addAttribute("user_contacts", contacts);
        return "user/search_users";
    }

    @PostMapping("/add_contact")
    @ResponseStatus(HttpStatus.OK)
    void addContact(@RequestParam("idContact") long idContact, Principal principal) {
        User user = castToUser(principal);
        User addUser = userService.addUserContact(user.getId(), idContact);
        if (addUser != null) {
            user.addContact(addUser);
            System.out.println(addUser);
        }
    }

    @PostMapping("/delete_contact")
    @ResponseStatus(HttpStatus.OK)
    void deleteContact(@RequestParam("idContact") long idContact, Principal principal) {
        User user = castToUser(principal);
        userService.deleteUserContact(user.getId(), idContact);
        user.deleteContact(idContact);
    }

    @GetMapping("/edit_my_data")
    String editUserData(Model model, Principal principal) {
        User user = castToUser(principal);
        model.addAttribute("user", user);
        model.addAttribute("userForm", new EditUserForm());
        model.addAttribute("errors", false);
        return "user/edit_user_data";
    }

    @PostMapping("/edit_my_data")
    String editUserData(@Valid @ModelAttribute("userForm") EditUserForm userForm,
                        BindingResult result,
                        Model model, Principal principal) {
        User user = castToUser(principal);
        model.addAttribute("user", user);
        if(result.hasErrors()){
            model.addAttribute("errors", true);
            return "user/edit_user_data";
        }
        user.setFirstName(userForm.getFirstName());
        user.setLastName(userForm.getLastName());
        user.setEmail(userForm.getEmail());
        userService.updateUserInfo(user);
        model.addAttribute("changeDataSuccess", true);
        return "user/edit_user_data";
    }

    @PostMapping("/change_password")
    String changePassword(Model model,
                          @NotNull @RequestParam("oldPassword") String oldPassword,
                          @NotNull @RequestParam("newPassword") String newPassword,
                          @NotNull @RequestParam("confirmPassword") String confirmPassword,
                          Principal principal, RedirectAttributes redirect) {
        User user = castToUser(principal);
        boolean success = true;
        if (!newPassword.equals(confirmPassword)) {
            redirect.addFlashAttribute("notEqualsPassword", "Passwords isn't equals!");
            success = false;
        }
        if (!bCryptPasswordEncoder.matches(oldPassword, user.getHashPassword())) {
            redirect.addFlashAttribute("incorrectOldPassword", "Old password isn't correct!");
            success = false;
        }
        if(newPassword.length() < 3 || newPassword.length() > 30){
            redirect.addFlashAttribute("incorrectNewPassword", "Password must have between 1 and 30 symbols");
            success = false;
        }
        if (success) {
            String hashPassword = bCryptPasswordEncoder.encode(newPassword);
            user.setHashPassword(hashPassword);
            userService.updateUserInfo(user);
            redirect.addFlashAttribute("changePasswordSuccess", true);
        }
        return "redirect:/edit_my_data";
    }

    @PostMapping("/delete_account")
    String deleteAccount(Principal principal) {
        User user = castToUser(principal);
        userService.lockUser(user.getId());
        logger.info("User with id = {} delete him account", user.getId());
        return "redirect:/logout";
    }
}

package messenger.controller;

import messenger.model.User;
import messenger.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    private User castToUser(Principal principal) {
        return (User) ((Authentication) principal).getPrincipal();
    }

    @GetMapping("/home_page")
    String toUserPage(Principal principal) {
        User user = (User) ((Authentication) principal).getPrincipal();
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
                       @RequestParam("login") String login,
                       @RequestParam("firstName") String firstName,
                       @RequestParam("lastName") String lastName) {
        List<User> users = userService.searchUsers(login, firstName, lastName);
        User user = castToUser(principal);
        List<String> contacts = user.getUserContacts().stream().map(User::getLogin).collect(Collectors.toList());
        model.addAttribute("searched_users", users);
        model.addAttribute("user_contacts", contacts);
        return "user/search_users";
    }

    @PostMapping("/add_contact")
    String addContact(@RequestParam("idContact") long idContact,
                      Principal principal) {
        User user = castToUser(principal);
        User addUser = userService.getMainUserInfo(idContact);
        if (userService.addUserContact(user.getId(), addUser.getId())) {
            user.addContact(addUser);
            System.out.println(addUser);
        }
        return "user/search_users";
    }

    @PostMapping("/delete_contact")
    String deleteContact(@RequestParam("idContact") long idContact,
                      Principal principal) {
        User user = castToUser(principal);
        userService.deleteUserContact(user.getId(), idContact);
        user.deleteContact(idContact);
        return "user/search_users";
    }

    @GetMapping("/edit_my_data")
    String editUserData(Model model, Principal principal) {
        User user = castToUser(principal);
        model.addAttribute("user", user);
        return "user/edit_user_data";
    }

    @PostMapping("/edit_my_data")
    String editUserData(@RequestParam("firstName") String firstName,
                        @RequestParam("lastName") String lastName,
                        @RequestParam("email") String email,
                        Principal principal) {
        User user = castToUser(principal);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        userService.updateUserInfo(user);
        return "redirect:/home_page";
    }

    @PostMapping("/change_password")
    String changePassword(Model model,
                          @RequestParam("oldPassword") String oldPassword,
                          @RequestParam("newPassword") String newPassword,
                          @RequestParam("confirmPassword") String confirmPassword,
                          Principal principal, RedirectAttributes redirect) {
        User user = castToUser(principal);
        if (!newPassword.equals(confirmPassword)) {
            redirect.addFlashAttribute("errorMessage", "Password isn't equals!");
        } else {
            if (!bCryptPasswordEncoder.matches(oldPassword, user.getHashPassword())){
                redirect.addFlashAttribute("errorMessage", "Old password isn't correct!");
            } else {
                String hashPassword = bCryptPasswordEncoder.encode(newPassword);
                user.setHashPassword(hashPassword);
                userService.updateUserInfo(user);
                redirect.addFlashAttribute("errorMessage", "Password change!");
            }
        }
        return "redirect:/edit_my_data";
    }

    @PostMapping("/delete_account")
    String deleteAccount(Principal principal) {
        User user = castToUser(principal);
        userService.lockUser(user.getId());
        return "redirect:/logout";
    }
}

package messenger.controller;

import messenger.model.RoleType;
import messenger.model.User;
import messenger.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserService userService;

    @GetMapping("/")
    private String adminPage() {
        return "admin/adminPage";
    }

    @GetMapping("/show_users")
    String showUsers(Model model) {
        List<User> users = userService.getAllUsers();
        users = users.stream().sorted(Comparator.comparing(User::getLogin)).collect(Collectors.toList());
        model.addAttribute("users", users);
        return "admin/showUsers";
    }

    @GetMapping("/user_details/{id}")
    String showUserDetails(Model model, @PathVariable("id") long idUser) {
        User user = userService.getUserCompletely(idUser);
        model.addAttribute("user", user);
        return "admin/userDetails";
    }

    @PostMapping("/lock_user")
    String lockUser(@RequestParam("idUser") long idUser) {
        userService.lockUser(idUser);
        logger.info("User with id = {} was locked", idUser);
        return "redirect:/admin/user_details/" + idUser;
    }

    @PostMapping("/unlock_user")
    String unlockUser(@RequestParam("idUser") long idUser) {
        userService.unlockUser(idUser);
        logger.info("User with id = {} was unlocked", idUser);
        return "redirect:/admin/user_details/" + idUser;
    }

    @PostMapping("/delete_role")
    String deleteRole(@RequestParam("authority") String authority,
                      @RequestParam("idUser") long idUser) {
        if (!authority.equals("ROLE_ADMIN") && !authority.equals("ROLE_USER")) {
            userService.deleteUserRole(idUser, RoleType.valueOf(authority));
            logger.info("Delete role = {} for user with id = {}", authority, idUser);
        }
        return "redirect:/admin/user_details/" + idUser;
    }

    @PostMapping("/grant_role")
    String grantRole(@RequestParam("authority") String authority,
                     @RequestParam("idUser") long idUser) {
        userService.grantRoleToUser(RoleType.valueOf(authority), idUser);
        logger.info("Grant role = {} for user with id = {}", authority, idUser);
        return "redirect:/admin/user_details/" + idUser;
    }
}

package messenger.controller;

import messenger.model.RoleType;
import messenger.model.User;
import messenger.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private UserService userService;

    @RolesAllowed({"ADMIN"})
    @GetMapping("/")
    private String adminPage() {
        return "admin/adminPage";
    }

    @GetMapping("/show_users")
    String showUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
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
        return "redirect:/admin/user_details/" + idUser;
    }

    @PostMapping("/unlock_user")
    String unlockUser(@RequestParam("idUser") long idUser) {
        userService.unlockUser(idUser);
        return "redirect:/admin/user_details/" + idUser;
    }

    @PostMapping("/delete_role")
    String deleteRole(@RequestParam("authority") String authority,
                      @RequestParam("idUser") long idUser) {
        if (!authority.equals("ROLE_ADMIN") && !authority.equals("ROLE_USER")) {
            userService.deleteUserRole(idUser, RoleType.valueOf(authority));
        }
        return "redirect:/admin/user_details/" + idUser;
    }

    @PostMapping("/grant_role")
    String grantRole(@RequestParam("authority") String authority,
                     @RequestParam("idUser") long idUser) {
        userService.grantRoleToUser(RoleType.valueOf(authority), idUser);
        return "redirect:/admin/user_details/" + idUser;
    }
}

package messenger.controller;

import messenger.controller.form.TalkForm;
import messenger.model.Message;
import messenger.model.Talk;
import messenger.model.User;
import messenger.services.TalkService;
import messenger.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class TalkController {
    @Autowired
    private UserService userService;

    @Autowired
    private TalkService talkService;

    private User castToUser(Principal principal) {
        return (User) ((Authentication) principal).getPrincipal();
    }

    @GetMapping("/user_talks")
    String showUserTalks(Model model, Principal principal) {
        User user = castToUser(principal);
        user.setTalks(userService.getUserTalks(user.getId()));
        List<Talk> talks = user.getTalks().stream()
                .sorted(Comparator.comparing(Talk::getName))
                .collect(Collectors.toList());
        model.addAttribute("user", user);
        model.addAttribute("talks", talks);
        return "talk/user_talks";
    }

    @GetMapping("/talk/{id}")
    String talk(Model model, Principal principal,
                @PathVariable("id") long idTalk,
                HttpServletRequest request, HttpServletResponse response) {
        User user = castToUser(principal);
        Talk talk = talkService.getTalkById(idTalk);
        if (talk != null &&
                (request.isUserInRole("ADMIN") || request.isUserInRole("SUPER_USER") ||
                        userService.isUserInTalk(user, idTalk))) {
            List<Message> messages = talkService.getMessages(idTalk);
            model.addAttribute("user", user);
            model.addAttribute("talk", talk);
            model.addAttribute("messages", messages);
        } else {
            return "redirect:/home_page";
        }
        return "talk/talk";
    }

    @PostMapping("/add_users")
    String addUsersInTalk(Model model, @RequestParam("idTalk") long idTalk) {
        Talk talk = talkService.getTalkById(idTalk);
        model.addAttribute("talk", talk);
        model.addAttribute("talkForm", new TalkForm());
        model.addAttribute("users", userService.getAllUsers());
        return "talk/add_users";
    }

    @PostMapping("/add_users_in_talk")
    String addUsersInTalk(@RequestParam("idTalk") long idTalk,
                          @ModelAttribute("talkForm") TalkForm talkForm,
                          Principal principal) {
        talkService.addUsersInTalk(idTalk, talkForm.getUsers());
        return "redirect:/user_talks";
    }

    @PostMapping("/delete_talk")
    String deleteTalk(Principal principal, @RequestParam("idTalk") long idTalk) {
        talkService.deleteUserFromTalk(idTalk, principal.getName());
        return "redirect:/user_talks";
    }

    @GetMapping("/create_talk")
    String create(Model model, Principal principal) {
        model.addAttribute("talkForm", new TalkForm());
        model.addAttribute("owner_login", principal.getName());
        model.addAttribute("users", userService.getAllUsers());
        return "talk/create_talk";
    }

    @PostMapping("/create_talk")
    String create(Principal principal, @ModelAttribute("talkForm") TalkForm talkForm) {
        User user = castToUser(principal);
        Talk talk = talkService.createTalk(talkForm.getName(), user.getLogin(), talkForm.getUsers());
        return "redirect:/talk/" + talk.getId();
    }

    @PostMapping("/send_message")
    String create(Principal principal, @RequestParam("idUser") long idUser) {
        User user = castToUser(principal);
        Talk talk = talkService.getDialog(user.getId(), idUser);
        return "redirect:/talk/" + talk.getId();
    }
}

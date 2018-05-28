package messenger.controller;

import messenger.controller.form.TalkForm;
import messenger.model.Message;
import messenger.model.Talk;
import messenger.model.User;
import messenger.services.TalkService;
import messenger.services.UserService;
import netscape.security.ForbiddenTargetException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
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
                HttpServletRequest request) {
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
            throw new ForbiddenTargetException();
        }
        return "talk/talk";
    }

    @PostMapping("/add_users")
    String addUsersInTalk(Model model, Principal principal,
                          @RequestParam("idTalk") long idTalk) {
        Talk talk = talkService.getTalkById(idTalk);
        User user = castToUser(principal);
        model.addAttribute("talk", talk);
        List<String> loginTalkUsers = talk.getUsers().stream().map(User::getLogin).collect(Collectors.toList());
        model.addAttribute("talkUsers", loginTalkUsers);
        model.addAttribute("talkForm", new TalkForm());
        model.addAttribute("users", user.getUserContacts());
        return "talk/add_users";
    }

    @PostMapping("/add_users_in_talk")
    String addUsersInTalk(@RequestParam("idTalk") long idTalk,
                          @ModelAttribute("talkForm") TalkForm talkForm,
                          Principal principal) {
        if (talkForm != null) {
            talkService.addUsersInTalk(idTalk, talkForm.getUsers());
        }
        return "redirect:/user_talks";
    }

    @PostMapping("/delete_talk")
    String deleteTalk(Principal principal, @RequestParam("idTalk") long idTalk) {
        talkService.deleteUserFromTalk(idTalk, principal.getName());
        return "redirect:/user_talks";
    }

    @GetMapping("/create_talk")
    String create(Model model, Principal principal) {
        User user = castToUser(principal);
        model.addAttribute("talkForm", new TalkForm());
        model.addAttribute("owner_login", user.getLogin());
        model.addAttribute("users", user.getUserContacts());
        return "talk/create_talk";
    }

    @PostMapping("/create_talk")
    String create(Principal principal, Model model,
                  @Valid @ModelAttribute("talkForm") TalkForm talkForm,
                  BindingResult result) {
        User user = castToUser(principal);
        if (result.hasErrors()) {
            model.addAttribute("users", user.getUserContacts());
            return "talk/create_talk";
        }
        Talk talk = talkService.createTalk(talkForm.getName(), user.getLogin(), talkForm.getUsers());
        user.addTalk(talk);
        return "redirect:/talk/" + talk.getId();
    }

    @PostMapping("/rename_talk")
    String renameTalk(@NotNull @RequestParam("nameTalk") String name,
                      @RequestParam("idTalk") long idTalk,
                      Principal principal) {
        if (name.length() > 0) {
            User user = castToUser(principal);
            if (userService.isUserInTalk(user, idTalk)) {
                Talk talk = talkService.getTalkById(idTalk);
                if (talk != null) {
                    talk.setName(name);
                    talkService.updateTalkInfo(talk);
                }
            }
        }
        return "redirect:/user_talks";
    }

    @PostMapping("/send_message")
    String create(Principal principal, @RequestParam("idUser") long idUser) {
        User user = castToUser(principal);
        Talk talk = talkService.getDialog(user.getId(), idUser);
        user.addTalk(talk);
        return "redirect:/talk/" + talk.getId();
    }
}

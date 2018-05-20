package messenger.controller;

import messenger.model.User;
import messenger.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping(value = "/users")
public class TestRestController {
    @Autowired
    private UserService userService;

    @RequestMapping(value = "/main", method = RequestMethod.GET)
    @ResponseBody
    public String indexClient() {
        RestTemplate rest = new RestTemplate();
        ResponseEntity<User> entity = rest.getForEntity("http://localhost:8080/users/show", User.class);
        return entity.getBody().getFirstName() + " " + entity.getBody().getLastName();
    }

    @RequestMapping(value = "/show", method = RequestMethod.GET)
    public User getUsers() {
        User user = userService.getUserCompletely("ivan");
        return user;
    }

}

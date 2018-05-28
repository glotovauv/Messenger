package messenger.controller.form;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

public class TalkForm {
    @NotNull
    @Size(min = 1, max = 30, message = "Name talk must have between 1 and 30 symbols")
    private String name;
    private List<String> users;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }
}

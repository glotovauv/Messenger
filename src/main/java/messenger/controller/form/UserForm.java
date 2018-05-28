package messenger.controller.form;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class UserForm {
    @NotNull
    @Size(min = 1, max = 30)
    @Pattern(regexp = "([a-zA-Z])([a-zA-Z0-9_])*",
            message = "Login can contains only latin letters, numbers and \'_\', first symbol must be a letter")
    private String login;

    @Valid
    private EditUserForm editUserData;

    @NotNull
    @Size(min = 3, max = 30, message = "Password must have between 1 and 30 symbols")
    private String password;

    @NotNull
    @Size(min = 3, max = 30, message = "Password must have between 1 and 30 symbols")
    private String confirmPassword;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getFirstName() {
        return editUserData.getFirstName();
    }

    public String getLastName() {
        return editUserData.getLastName();
    }

    public String getEmail() {
        return editUserData.getEmail();
    }

    public EditUserForm getEditUserData() {
        return editUserData;
    }

    public void setEditUserData(EditUserForm editUserData) {
        this.editUserData = editUserData;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getPassword() {
        return password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }
}

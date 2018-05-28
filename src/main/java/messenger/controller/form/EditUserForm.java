package messenger.controller.form;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class EditUserForm {
    @NotNull
    @Size(min = 1, max = 30,
            message = "First name must have between 1 and 30 symbols")
    private String firstName;

    @NotNull
    @Size(min = 1, max = 30,
            message = "Last name must have between 1 and 30 symbols")
    private String lastName;

    @NotNull
    @Size(min = 1, max = 30, message = "Email must have between 1 and 30 symbols")
    @Email(message = "Email incorrect")
    private String email;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

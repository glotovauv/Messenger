package messenger.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class Author {
    @NotNull
    private long id;

    @Size(min = 1, max = 30, message = "First name must have between 1 and 30 symbols")
    private String firstName;

    @Size(min = 1, max = 30, message = "Last name must have between 1 and 30 symbols")
    private String lastName;

    protected Author() {
    }

    public Author(long id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Author(User user){
        this.id = user.getId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
    }

    public long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    @Override
    public String toString() {
        return "Author{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }
}

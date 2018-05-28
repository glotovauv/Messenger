package messenger.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull
    @Size(min = 1, max = 30)
    @Column(unique = true, nullable = false)
    private String login;

    @NotNull
    @Column(name = "hash_password", nullable = false)
    private String hashPassword;

    @NotNull
    @Size(min = 1, max = 30)
    @Column(name = "first_name")
    private String firstName;

    @NotNull
    @Size(min = 1, max = 30)
    @Column(name = "last_name")
    private String lastName;

    @NotNull
    @Email
    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private boolean active;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH, mappedBy = "users")
    private Set<Talk> talks;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    @JoinTable(name = "contacts",
            joinColumns = @JoinColumn(name = "id_user"),
            inverseJoinColumns = @JoinColumn(name = "id_contact_user")
    )
    private Set<User> userContacts = new HashSet<>();

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_role",
            joinColumns = @JoinColumn(name = "id_user", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "id_role", referencedColumnName = "id")
    )
    private Set<Role> roles = new HashSet<>();

    public User() {
    }

    public long getId() {
        return id;
    }

    public Set<Talk> getTalks() {
        return talks;
    }

    public void deleteTalk(long idTalk) {
        Iterator<Talk> talkIterator = this.talks.iterator();
        while (talkIterator.hasNext()) {
            Talk talk = talkIterator.next();
            if (talk.getId() == idTalk) {
                talkIterator.remove();
                break;
            }
        }
    }

    public void addTalk(Talk talk) {
        this.talks.add(talk);
    }

    public String getLogin() {
        return login;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getHashPassword() {
        return hashPassword;
    }

    public String getEmail() {
        return email;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setTalks(Set<Talk> talks) {
        this.talks = talks;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Set<User> getUserContacts() {
        return userContacts;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void addContact(User user) {
        userContacts.add(user);
    }

    public void deleteContact(long idContact){
        Iterator<User> userIterator = this.userContacts.iterator();
        while (userIterator.hasNext()) {
            User user = userIterator.next();
            if (user.getId() == idContact) {
                userIterator.remove();
                break;
            }
        }
    }

    public void addRole(Role role) {
        roles.add(role);
    }

    public User(String login, String hashPassword, String firstName, String lastName, String email) {
        this.login = login;
        this.hashPassword = hashPassword;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.active = true;
    }

    @Override
    public String toString() {
        return String.format(
                "User[login='%s', firstName='%s', lastName='%s']",
                login, firstName, lastName);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }

    @Override
    public String getPassword() {
        return getHashPassword();
    }

    @Override
    public String getUsername() {
        return getLogin();
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setHashPassword(String hashPassword) {
        this.hashPassword = hashPassword;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

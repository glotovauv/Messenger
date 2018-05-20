package messenger.model;

import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "roles")
public class Role implements GrantedAuthority {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String authority;

    protected Role(){}

    @ManyToMany(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY, mappedBy = "roles")
    private Set<User> users;

    public Role(String authority, Set<User> users) {
        this.authority = authority;
        this.users = users;
    }

    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", authority='" + authority + '\'' +
                '}';
    }

    public long getId() {
        return id;
    }

    @Override
    public String getAuthority() {
        return authority;
    }
}

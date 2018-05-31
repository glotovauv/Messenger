package messenger.services;

import messenger.model.Role;
import messenger.model.RoleType;
import messenger.model.Talk;
import messenger.model.User;
import messenger.repository.RoleRepository;
import messenger.repository.UserRepository;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service("userDetailsService")
public class UserServiceImpl implements UserService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void updateUserInfo(User user) {
        userRepository.save(user);
    }

    @Override
    @Transactional
    public boolean createUser(User user) {
        if (userRepository.existsByLogin(user.getLogin())) {
            return false;
        }
        Role role = roleRepository.findRoleByAuthority(RoleType.ROLE_USER.toString());
        user.addRole(role);
        userRepository.save(user);
        return true;
    }

    @Override
    public void lockUser(long idUser) {
        changeActiveUser(idUser, false);
    }

    @Override
    public void unlockUser(long idUser) {
        changeActiveUser(idUser, true);
    }

    @Transactional
    void changeActiveUser(long idUser, boolean active) {
        User user = userRepository.findById(idUser).orElse(null);
        if (user != null) {
            user.setActive(active);
            userRepository.save(user);
        }
    }

    @Override
    @Transactional
    public User getUserCompletely(String login) {
        User user = userRepository.findUserByLogin(login);
        if (user != null) {
            Hibernate.initialize(user.getTalks());
            Hibernate.initialize(user.getUserContacts());
            Hibernate.initialize(user.getRoles());
        }
        return user;
    }

    @Override
    @Transactional
    public User getUserCompletely(long idUser) {
        User user = userRepository.findById(idUser).orElse(null);
        if (user != null) {
            Hibernate.initialize(user.getTalks());
            Hibernate.initialize(user.getUserContacts());
            Hibernate.initialize(user.getRoles());
        }
        return user;
    }

    @Override
    public User getMainUserInfo(long idUser) {
        return userRepository.findById(idUser).orElse(null);
    }

    @Override
    public List<User> getAllUsers() {
        return (List<User>) userRepository.findAll();
    }

    @Override
    @Transactional
    public User addUserContact(long idUser, long idContactUser) {
        User user = userRepository.findById(idUser).orElse(null);
        User addUser = userRepository.findById(idContactUser).orElse(null);
        if (user == null || addUser == null) {
            return null;
        }
        user.addContact(addUser);
        userRepository.save(user);
        return addUser;
    }

    @Override
    @Transactional
    public void deleteUserContact(long idUser, long idContactUser) {
        userRepository.deleteContact(idUser, idContactUser);
    }

    @Override
    @Transactional
    public boolean grantRoleToUser(RoleType roleType, long idUser) {
        User user = userRepository.findById(idUser).orElse(null);
        if (user == null) {
            return false;
        }
        Role role = roleRepository.findRoleByAuthority(roleType.toString());
        user.addRole(role);
        userRepository.save(user);
        return true;
    }

    @Override
    @Transactional
    public void deleteUserRole(long idUser, RoleType roleType) {
        Role role = roleRepository.findRoleByAuthority(roleType.toString());
        userRepository.deleteRole(idUser, role.getId());
    }

    @Override
    public List<User> searchUsers(String login, String firstName, String lastName) {
        return userRepository.findDistinctByLoginOrFirstNameOrLastName(login, firstName, lastName);
    }

    @Override
    public boolean isUserInTalk(User user, long idTalk) {
        Set<Talk> talks = user.getTalks();
        return talks.stream().anyMatch(talk -> talk.getId() == idTalk);
    }

    @Override
    public boolean isUserInContact(User user, long idUserContact) {
        Set<User> contacts = user.getUserContacts();
        return contacts.stream().anyMatch(contact -> contact.getId() == idUserContact);
    }

    @Override
    public boolean isUserInRole(User user, RoleType roleType) {
        Set<Role> roles = user.getRoles();
        return roles.stream().anyMatch(role -> role.getAuthority().equals(roleType.toString()));
    }

    @Override
    public Set<Talk> getUserTalks(long idUser) {
        User user = userRepository.findById(idUser).orElse(null);
        return user == null ? null : user.getTalks();
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        User user = getUserCompletely(login);
        if (user == null) {
            throw new UsernameNotFoundException("Can't find user with login " + login);
        }
        return user;
    }
}



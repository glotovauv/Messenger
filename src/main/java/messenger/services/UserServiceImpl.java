package messenger.services;

import messenger.model.Role;
import messenger.model.RoleType;
import messenger.model.Talk;
import messenger.model.User;
import messenger.repository.RoleRepository;
import messenger.repository.UserRepository;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service("userDetailsService")
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void updateUserInfo(User user) {
        userRepository.save(user);
    }

    @Override
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
    public void unlockUser(long idUser){
        changeActiveUser(idUser, true);
    }

    private void changeActiveUser(long idUser, boolean active){
        User user = userRepository.findById(idUser).orElse(null);
        if(user != null){
            user.setActive(active);
            userRepository.save(user);
        }
    }

    @Override
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
    public boolean addUserContact(long idUser, long idContactUser) {
        User user = userRepository.findById(idUser).orElse(null);
        User addUser = userRepository.findById(idContactUser).orElse(null);
        if (user == null || addUser == null) {
            return false;
        }
        user.addContact(addUser);
        userRepository.save(user);
        return true;
    }

    @Override
    public void deleteUserContact(long idUser, long idContactUser){
        userRepository.deleteContact(idUser, idContactUser);
    }

    @Override
    public boolean grantRoleToUser(RoleType roleType, long idUser) {
        User user = userRepository.findById(idUser).orElse(null);
        if (user == null) {
            return false;
        }
        //Hibernate.initialize(user.getRoles());
        Role role = roleRepository.findRoleByAuthority(roleType.toString());
        user.addRole(role);
        userRepository.save(user);
        return true;
    }

    @Override
    public void deleteUserRole(long idUser, RoleType roleType){
        Role role = roleRepository.findRoleByAuthority(roleType.toString());
        userRepository.deleteRole(idUser, role.getId());
    }

    @Override
    public List<User> searchUsers(String login, String firstName, String lastName){
        return userRepository.findDistinctByLoginOrFirstNameOrLastName(login, firstName, lastName);
    }

    @Override
    public boolean isUserInTalk(User user, long idTalk){
        Set<Talk> talks = user.getTalks();
        return talks.stream().anyMatch(talk -> talk.getId() == idTalk);
    }

    @Override
    public boolean isUserInContact(User user, long idUserContact){
        Set<User> contacts = user.getUserContacts();
        return contacts.stream().anyMatch(contact -> contact.getId() == idUserContact);
    }

    @Override
    public Set<Talk> getUserTalks(long idUser){
        User user = userRepository.findById(idUser).orElse(null);
        return user == null ? null : user.getTalks();
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        User user = getUserCompletely(login);
        if (user == null) {
            throw new UsernameNotFoundException("Can't find user with login " + login);
        }
        return user;
    }
}



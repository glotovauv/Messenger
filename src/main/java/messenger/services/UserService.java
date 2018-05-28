package messenger.services;

import messenger.model.RoleType;
import messenger.model.Talk;
import messenger.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Set;

public interface UserService extends UserDetailsService {
    void updateUserInfo(User user);

    boolean createUser(User user);

    void lockUser(long idUser);

    void unlockUser(long idUser);

    User getUserCompletely(String login);

    User getUserCompletely(long idUser);

    User getMainUserInfo(long idUser);

    List<User> getAllUsers();

    User addUserContact(long idUser, long idContactUser);

    void deleteUserContact(long idUser, long idContactUser);

    boolean grantRoleToUser(RoleType roleType, long idUser);

    void deleteUserRole(long idUser, RoleType roleType);

    List<User> searchUsers(String login, String firstName, String lastName);

    boolean isUserInTalk(User user, long idTalk);

    boolean isUserInContact(User user, long idUserContact);

    boolean isUserInRole(User user, RoleType roleType);

    Set<Talk> getUserTalks(long idUser);
}

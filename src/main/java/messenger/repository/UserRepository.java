package messenger.repository;

import messenger.model.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    User findUserByLogin(String login);

    boolean existsByLogin(String login);

    void deleteByLogin(String login);

    List<User> findDistinctByLoginOrFirstNameOrLastName(String login, String firstName, String lastName);

    @Modifying
    @Query(value = "delete from contacts where id_user = :id_user and id_contact_user = :id_contact_user",
            nativeQuery = true)
    void deleteContact(@Param("id_user") long id_user, @Param("id_contact_user") long id_contact_user);

    @Modifying
    @Query(value = "delete from user_role where id_user = :id_user and id_role = :id_role",
            nativeQuery = true)
    void deleteRole(@Param("id_user") long id_user, @Param("id_role") long id_role);
}

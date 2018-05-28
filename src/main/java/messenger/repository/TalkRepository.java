package messenger.repository;

import messenger.model.Talk;
import messenger.model.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface TalkRepository extends CrudRepository<Talk, Long> {
    List<Talk> findTalksByNameAndOwner(String name, User owner);

    List<Talk> findByUsers(User user);

    boolean existsByNameAndOwner(String name, User owner);

    void deleteById(long id);
}

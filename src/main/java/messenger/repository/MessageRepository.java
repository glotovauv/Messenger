package messenger.repository;

import messenger.model.Message;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MessageRepository extends MongoRepository<Message, String> {
    List<Message> findByIdTalk(long idTalk);

    void deleteByIdTalk(long idTalk);
}

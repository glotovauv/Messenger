package messenger.services;

import messenger.model.Message;
import messenger.model.Talk;
import messenger.model.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Date;
import java.util.List;

public interface TalkService {
    void updateTalkInfo(Talk talk);

    Talk createTalk(String name, String loginOwner);

    Talk createTalk(String name, String loginOwner, List<String> loginUsers);

    Talk getTalkById(long idTalk);

    boolean addUsersInTalk(long idTalk, List<String> loginUsers);

    boolean deleteUserFromTalk(long idTalk, String login);

    Message writeMessage(Message message, long idTalk);

    void writeMessageWithFile(Message message, Talk talk, MultipartFile file);

    Message writeMessageWithFile(Message message, Talk talk, String fileInBase64);

    boolean writeFile(MultipartFile file, long idTalk, long idAuthor, Date date);

    File getFileFromMessage(long idTalk, String idMessage);

    List<Message> getMessages(long idTalk);

    void deleteMessageById(String idMessage);

    void deleteTalk(long idTalk);

    List<Talk> getTalks(String userLogin);

    Talk getDialog(long idFirstUser, long idSecondUser);
}

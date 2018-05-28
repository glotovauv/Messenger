package messenger.services;

import messenger.model.Message;
import messenger.model.Talk;
import messenger.model.User;
import messenger.repository.MessageRepository;
import messenger.repository.TalkRepository;
import messenger.repository.UserRepository;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sun.misc.BASE64Decoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
public class TalkServiceImpl implements TalkService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private TalkRepository talkRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public void updateTalkInfo(Talk talk) {
        talkRepository.save(talk);
    }

    @Override
    public Talk createTalk(String name, String loginOwner) {
        return createTalk(name, loginOwner, null);
    }

    @Override
    @Transactional
    public Talk createTalk(String name, String loginOwner, List<String> loginUsers) {
        User owner = userRepository.findUserByLogin(loginOwner);
        if (owner == null || talkRepository.existsByNameAndOwner(name, owner)) {
            return null;
        }
        Talk talk = new Talk(name, owner);
        talk.addUser(owner);
        if (loginUsers != null) {
            for (String login : loginUsers) {
                User user = userRepository.findUserByLogin(login);
                if (user != null) {
                    talk.addUser(user);
                }
            }
        }
        talkRepository.save(talk);
        return talk;
    }

    @Override
    @Transactional
    public Talk getTalkById(long idTalk) {
        Talk talk = talkRepository.findById(idTalk).orElse(null);
        if (talk != null) {
            Hibernate.initialize(talk.getUsers());
        }
        return talk;
    }

    @Override
    @Transactional
    public boolean addUsersInTalk(long idTalk, List<String> loginUsers) {
        Talk talk = talkRepository.findById(idTalk).orElse(null);
        if (talk == null) {
            return false;
        }
        for (String login : loginUsers) {
            User user = userRepository.findUserByLogin(login);
            if (user == null) {
                return false;
            }
            talk.addUser(user);
        }
        talkRepository.save(talk);
        return true;
    }

    @Override
    @Transactional
    public boolean deleteUserFromTalk(long idTalk, String login) {
        User user = userRepository.findUserByLogin(login);
        Talk talk = talkRepository.findById(idTalk).orElse(null);
        if (user == null || talk == null) {
            return false;
        }
        Set<User> users = talk.getUsers();
        if (users.contains(user)) {
            if (users.size() == 1) {
                deleteTalk(talk.getId());
            } else {
                users.remove(user);
                talkRepository.save(talk);
            }
        }
        return true;
    }

    @Override
    public void deleteTalk(long idTalk) {
        File talkDirectory = new File(getTalkDirectory(idTalk));
        deleteDirectory(talkDirectory);
        messageRepository.deleteByIdTalk(idTalk);
        talkRepository.deleteById(idTalk);
    }

    private void deleteDirectory(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    deleteDirectory(f);
                }
            }
        }
        file.delete();
    }

    @Override
    public Message writeMessage(Message message, long idTalk) {
        if (talkRepository.existsById(idTalk)) {
            message.setIdTalk(idTalk);
            message = messageRepository.save(message);
        }
        return message;
    }

    @Override
    public void writeMessageWithFile(Message message, Talk talk, MultipartFile file) {
        if (writeFile(file, talk.getId(), message.getAuthor().getId(), message.getDate())) {
            message.setNameAttachedFile(file.getOriginalFilename());
        } else {
            message.setNameAttachedFile("");
        }
        writeMessage(message, talk.getId());
    }

    @Override
    public Message writeMessageWithFile(Message message, Talk talk, String fileInBase64) {
        fileInBase64 = deleteHeader(fileInBase64);
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            byte[] bytes = decoder.decodeBuffer(fileInBase64);
            writeFile(message.getNameAttachedFile(), bytes, talk.getId(), message.getAuthor().getId(), message.getDate());
        } catch (IOException e) {
            logger.error("Ошибка при загрузке: {}", e.getMessage());
            message.setNameAttachedFile("");
        }
        return writeMessage(message, talk.getId());
    }

    @Override
    public boolean writeFile(MultipartFile file, long idTalk, long idAuthor, Date date) {
        if (!file.isEmpty()) {
            try {
                byte[] bytes = file.getBytes();
                writeFile(file.getOriginalFilename(), bytes, idTalk, idAuthor, date);
                return true;
            } catch (Exception e) {
                logger.error("Ошибка при загрузке: {}", e.getMessage());
            }
        }
        return false;
    }

    private void writeFile(String nameFile, byte[] content, long idTalk, long idAuthor, Date date) throws IOException {
        String path = getPathToMessageFile(idTalk, idAuthor, date);
        File directory = new File(path);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created) {
                throw new IOException();
            }
        }
        path += nameFile;
        File uploadFile = new File(path);
        FileOutputStream stream = new FileOutputStream(uploadFile);
        stream.write(content);
        stream.close();
    }

    private String deleteHeader(String fileInBase64) {
        int boundHeader = fileInBase64.lastIndexOf(',');
        return boundHeader > 0 ? fileInBase64.substring(boundHeader + 1) : fileInBase64;
    }

    @Override
    public File getFileFromMessage(long idTalk, String idMessage) {
        Message message = messageRepository.findById(idMessage).orElse(null);
        if (message == null) {
            return null;
        }
        String path = getPathToMessageFile(idTalk, message) + message.getNameAttachedFile();
        return new File(path);
    }

    @Override
    public List<Message> getMessages(long idTalk) {
        return messageRepository.findByIdTalk(idTalk);
    }

    @Override
    public boolean deleteMessageById(String idMessage, long idUser) {
        Message message = messageRepository.findById(idMessage).orElse(null);
        if (message != null) {
            if (message.getAuthor().getId() != idUser) {
                return false;
            }
            if (message.getNameAttachedFile() != null && !message.getNameAttachedFile().equals("")) {
                String path = getPathToMessageFile(message.getIdTalk(), message);
                File file = new File(path);
                deleteDirectory(file);
            }
            messageRepository.deleteById(idMessage);
            return true;
        }
        return false;
    }

    @Override
    public List<Talk> getTalks(String userLogin) {
        User user = userRepository.findUserByLogin(userLogin);
        return user == null ? null : talkRepository.findByUsers(user);
    }

    @Override
    @Transactional
    public Talk getDialog(long idFirstUser, long idSecondUser) {
        User firstUser = userRepository.findById(idFirstUser).orElse(null);
        User secondUser = userRepository.findById(idSecondUser).orElse(null);
        if (firstUser == null || secondUser == null) {
            return null;
        }
        Talk talk = findDialogByUsers(firstUser, secondUser);
        if (talk == null) {
            talk = findDialogByUsers(secondUser, firstUser);
            if (talk == null) {
                talk = new Talk(firstUser.getFullName() + " - " + secondUser.getFullName(), firstUser);
                talk.addUser(firstUser);
                talk.addUser(secondUser);
                talk = talkRepository.save(talk);
            }
        }
        return talk;
    }

    private Talk findDialogByUsers(User firstUser, User secondUser) {
        String nameTalk = firstUser.getFullName() + " - " + secondUser.getFullName();
        List<Talk> talks = talkRepository.findTalksByNameAndOwner(nameTalk, firstUser);
        return talks.stream()
                .filter(talk -> talk.getUsers().size() == 2 &&
                        talk.getUsers().contains(firstUser) &&
                        talk.getUsers().contains(secondUser))
                .findFirst().orElse(null);
    }

    private String getPathToMessageFile(long idTalk, Message message) {
        return getPathToMessageFile(idTalk, message.getAuthor().getId(), message.getDate());
    }

    private String getPathToMessageFile(long idTalk, long idAuthor, Date date) {
        DateFormat dateFormat = new SimpleDateFormat("d.MM.yyyy HH.mm.ss");
        return getTalkDirectory(idTalk) + idAuthor + File.separator +
                dateFormat.format(date) + File.separator;
    }

    private String getTalkDirectory(long idTalk) {
        return "Talks" + File.separator + idTalk + File.separator;
    }
}

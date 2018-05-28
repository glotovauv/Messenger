package messenger.controller;

import messenger.controller.form.MessageForm;
import messenger.exception.ResourceNotFoundException;
import messenger.model.Author;
import messenger.model.Message;
import messenger.model.User;
import messenger.services.TalkService;
import messenger.services.UserService;
import netscape.security.ForbiddenTargetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileInputStream;
import java.security.Principal;
import java.util.Date;

@Controller
public class MessageController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserService userService;

    @Autowired
    private TalkService talkService;

    @MessageMapping("/talk/{id}/send_message")
    @SendTo("/topic/talk/{id}")
    public MessageForm sendMessage(@Valid @Payload MessageForm messageForm, Principal principal, @DestinationVariable("id") long idTalk) {
        long idAuthor = messageForm.getMessage().getAuthor().getId();
        User author = (User) ((Authentication) principal).getPrincipal();
        if (author.getId() != idAuthor) {
            author = userService.getMainUserInfo(idAuthor);
        }

        Message message = messageForm.getMessage();
        message.setAuthor(new Author(author));
        message = talkService.writeMessage(message, idTalk);
        messageForm.setMessage(message);
        return messageForm;
    }

    @MessageMapping("/talk/{id}/delete_message")
    @SendTo("/topic/talk/{id}")
    public MessageForm deleteMessage(@NotNull @Payload MessageForm messageForm,
                                     @DestinationVariable("id") long idTalk,
                                     Principal principal) {
        if (messageForm.getMessage() != null) {
            User author = (User) ((Authentication) principal).getPrincipal();
            if (!talkService.deleteMessageById(messageForm.getMessage().getId(), author.getId())) {
                logger.warn("User with id = {} can delete message with id = {} in talk with id = {} without permission",
                        author.getId(), idTalk, messageForm.getMessage().getId());
                throw new ForbiddenTargetException("Only author can delete message");
            }
        }
        return messageForm;
    }

    @PostMapping("/download_file")
    ResponseEntity<InputStreamResource> downloadFile(@RequestParam("idTalk") long idTalk,
                                                     @RequestParam("idMessage") String idMessage,
                                                     HttpServletRequest request) {
        File file = talkService.getFileFromMessage(idTalk, idMessage);
        InputStreamResource inputStream;
        try {
            inputStream = new InputStreamResource(new FileInputStream(file));
        } catch (Exception e) {
            logger.error("Resource for message with id = {} in talk with id = {} not found",
                    idMessage, idTalk);
            throw new ResourceNotFoundException();
        }
        String mimeType = request.getServletContext().getMimeType(file.getAbsolutePath());
        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(mimeType);
        } catch (Exception e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + file.getName())
                .contentType(mediaType)
                .contentLength(file.length())
                .body(inputStream);
    }

    @PostMapping("/upload_file")
    @ResponseStatus(value = HttpStatus.OK)
    void uploadFile(@RequestParam("idUser") long idUser,
                    @RequestParam("idTalk") long idTalk,
                    @RequestParam("date") Date date,
                    @RequestParam("file") MultipartFile file) {
        talkService.writeFile(file, idTalk, idUser, date);
    }
}

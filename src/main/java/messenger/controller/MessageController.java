package messenger.controller;

import messenger.controller.form.MessageForm;
import messenger.controller.form.TalkForm;
import messenger.model.Author;
import messenger.model.Message;
import messenger.model.Talk;
import messenger.model.User;
import messenger.services.TalkService;
import messenger.services.UserService;
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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import sun.misc.BASE64Decoder;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.Date;

@Controller
public class MessageController {
    @Autowired
    private UserService userService;

    @Autowired
    private TalkService talkService;

    @MessageMapping("/talk/{id}/send_message")
    @SendTo("/topic/talk/{id}")
    public MessageForm sendMessage(@Payload MessageForm messageForm, @DestinationVariable("id") long idTalk) {
        System.out.println("in MessageMapping");
        User author = userService.getMainUserInfo(messageForm.getMessage().getAuthor().getId());

        Message message = messageForm.getMessage();
        message.setAuthor(new Author(author));
        message = talkService.writeMessage(message, idTalk);
        messageForm.setMessage(message);
        return messageForm;
    }

    @MessageMapping("/talk/{id}/delete_message")
    @SendTo("/topic/talk/{id}")
    public MessageForm deleteMessage(@Payload MessageForm messageForm, @DestinationVariable("id") long idTalk) {
        talkService.deleteMessageById(messageForm.getMessage().getId());
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
            return ResponseEntity.notFound().build();
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

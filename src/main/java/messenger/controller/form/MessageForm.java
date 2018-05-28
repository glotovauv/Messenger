package messenger.controller.form;

import messenger.model.Message;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class MessageForm {
    @NotNull
    private MessageType typeMessage;

    @Valid
    private Message message;

    public enum MessageType {
        SEND,
        DELETE
    }

    public MessageType getTypeMessage() {
        return typeMessage;
    }

    public void setTypeMessage(MessageType typeMessage) {
        this.typeMessage = typeMessage;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}

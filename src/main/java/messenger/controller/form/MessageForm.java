package messenger.controller.form;

import messenger.model.Message;

public class MessageForm {
    private MessageType typeMessage;
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

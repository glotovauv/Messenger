package messenger.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

@Document
public class Message {
    @Id
    private String id;

    private long idTalk;

    @Size(max = 255, message = "Size message should be less than 255 symbols")
    private String text;

    @NotNull
    private Date date;

    @Valid
    private Author author;

    @Size(max = 30, message = "Name file should be less 30 symbols")
    private String nameAttachedFile;

    public Message(String text, Date date, Author author) {
        this.text = text;
        this.date = date;
        this.author = author;
        nameAttachedFile = "";
    }

    public Message() {
    }

    public String getId() {
        return id;
    }

    public long getIdTalk() {
        return idTalk;
    }

    public void setIdTalk(long idTalk) {
        this.idTalk = idTalk;
    }

    public String getText() {
        return text;
    }

    public Date getDate() {
        return date;
    }

    public Author getAuthor() {
        return author;
    }

    public String getNameAttachedFile() {
        return nameAttachedFile;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public void setNameAttachedFile(String nameAttachedFile) {
        this.nameAttachedFile = nameAttachedFile;
    }

    @Override
    public String toString() {
        return "\nMessage from " + author.getFirstName() + " " + author.getLastName() +
                " (" + date + "):\n" + text + "\n";
    }
}

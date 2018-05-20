package messenger.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document
public class Message {
    @Id
    private String id;
    private long idTalk;
    private String text;
    private Date date;
    private Author author;
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

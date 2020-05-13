package ChatClient.src.com.cp;

import java.sql.Timestamp;

public class Message {
    private String author;
    private String sendTo;
    private String msg;
    private Timestamp time;

    public Message(String author, String sendTo, String msg, Timestamp time) {
        this.author = author;
        this.sendTo = sendTo;
        this.msg = msg;
        this.time = time;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getSendTo() {
        return sendTo;
    }

    public void setSendTo(String sendTo) {
        this.sendTo = sendTo;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}

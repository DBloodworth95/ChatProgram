package ChatClient.src.com.cp;

import java.sql.Timestamp;

public class Message {
    private String author;
    private String sendTo;
    private String msg;
    private Timestamp time;
    public static class Builder{
        private String author;
        private String sendTo;
        private String msg;
        private Timestamp time;

        public Builder() {

        }
        public Builder author(String val) {
            author = val;
            return this;
        }
        public Builder sendTo(String val) {
            sendTo = val;
            return this;
        }
        public Builder msg(String val) {
            msg = val;
            return this;
        }
        public Builder time(Timestamp val) {
            time = val;
            return this;
        }
        public Message build() {
            return new Message(this);
        }
    }
    public Message(Builder builder) {
        author = builder.author;
        sendTo = builder.sendTo;
        msg = builder.msg;
        time = builder.time;
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

package ChatClient.src.com.cp;

import java.sql.Timestamp;

public class Message {
    private final String author;
    private final String sendTo;
    private final String msg;
    private final Timestamp time;

    public static class Builder {
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


    public Timestamp getTime() {
        return time;
    }

    public String getMsg() {
        return msg;
    }
}

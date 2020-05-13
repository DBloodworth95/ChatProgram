package ChatClient.src.com.cp;

public interface UserStatusListener {
    public void online(String login);
    public void offline(String login);
}

package clark.dynamicproxyexercise.cglib;

/**
 * ClassName: SendMessage
 * Package: com.clark.dynamicproxyexercise.cglib
 */
public class SendMessage {
    public String send(String message) {
        System.out.println("send message:" + message);
        return message;
    }
}

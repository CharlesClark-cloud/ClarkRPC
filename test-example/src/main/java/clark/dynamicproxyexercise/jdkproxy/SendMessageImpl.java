package clark.dynamicproxyexercise.jdkproxy;

/**
 * ClassName: SendMessageImpl
 * Package: com.clark.dynamicproxyexercise
 */
public class SendMessageImpl implements  SendMessage{
    @Override
    public String send(String message){
        System.out.println("send message:" + message);
        return message;
    }
}

package clark.dynamicproxyexercise.jdkproxy;

/**
 * ClassName: ProxyTest
 * Package: com.clark.dynamicproxyexercise
 */
public class ProxyTest {
    public static void main(String[] args) {
        SendMessage smsService = (SendMessage) JdkProxyFactory.getProxy(new SendMessageImpl());
        smsService.send("java");

    }
}

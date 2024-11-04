package clark.dynamicproxyexercise.cglib;

/**
 * ClassName: ProxyTest
 * Package: com.clark.dynamicproxyexercise.cglib
 */
public class ProxyTest {
    public static void main(String[] args) {
        SendMessage smsService = (SendMessage) CglibProxyFactory.getProxy(SendMessage.class);
        smsService.send("java");
    }

}

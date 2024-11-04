package clark.sokcetexercise;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * ClassName: HelloClient
 * Package: clark.test
 */
public class HelloClient {
    private static final Logger logger = LoggerFactory.getLogger(HelloClient.class);

    public Object send(String  message, String host, int port) {
        //1. 创建Socket对象并且指定服务器的地址和端口号
        try (Socket socket = new Socket(host, port)) {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            //2.通过输出流向服务器端发送请求信息
            objectOutputStream.writeObject(message);
            //3.通过输入流获取服务器响应的信息
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            return objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            logger.error("occur exception:", e);
        }
        return null;
    }

    public static void main(String[] args) {
        HelloClient[] helloClient = new HelloClient[100];
//        String message = (String) helloClient.send("content from client", "127.0.0.1", 6666);
//        System.out.println("client receive message:" + message);
        for(int i = 0;i<100;i++){
            helloClient[i] = new HelloClient();
            System.out.println((i+1)+" 号client receive message:" +helloClient[i].send("content from client", "127.0.0.1", 6666));
        }
    }private static class ClientHandler implements Runnable {
        private final Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                 ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())) {

                // 读取客户端发送的请求信息
                String message = (String) objectInputStream.readObject();
                logger.info("Server received message: " + message);

                // 向客户端发送响应信息
                objectOutputStream.writeObject("Return Message");
                objectOutputStream.flush();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}

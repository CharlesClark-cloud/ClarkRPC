package clark.sokcetexercise;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * ClassName: HelloServer
 * Package: clark.test
 */
public class HelloServer {
    private static final Logger logger = LoggerFactory.getLogger(HelloServer.class);
    public  static  final ExecutorService threadPool = new ThreadPoolExecutor(
            10,
            50,
            1L,
            TimeUnit.MINUTES,
            new ArrayBlockingQueue<>(200),
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

//    public void start(int port){
//        try(ServerSocket server = new ServerSocket(port);) {
//            Socket socket;
//            int count = 0;
//            while ((socket = server.accept())!=null){
//                count++;
//                System.out.println(count+" 号建立连接！");
//                //证明监听到请求
//                try (ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
//                     ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())) {
//                    //3.通过输入流读取客户端发送的请求信息
//                    String message = (String) objectInputStream.readObject();
//                    logger.info("server receive message:" + message);
//
//                    //4.通过输出流向客户端发送响应信息
//                    objectOutputStream.writeObject("Return Message");
//                    objectOutputStream.flush();
//
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//
//            }
//
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//    }
    public  void  start(int port){
        try(ServerSocket server = new ServerSocket(port);){
            int count = 0;
            while (true){
                Socket socket = server.accept();
                count++;
                System.out.println(count + " 号建立连接！");
                threadPool.execute(new ClientHandler(socket));

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private static class ClientHandler implements Runnable {
        private final Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            long threadId = Thread.currentThread().getId();
            System.out.println("当前处理任务的线程编号: " + threadId);
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
    public static void main(String[] args) {
        HelloServer helloServer = new HelloServer();
        helloServer.start(6666);
    }

}

package clarkrpc.registry.zookeepertest;

import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.KeeperException;
import java.io.IOException;
import java.util.List;

public class ZookeeperClient {

    private static final int SESSION_TIMEOUT = 3000; // 设置连接超时时间
    private ZooKeeper zooKeeper;

    public void connect(String hosts) throws IOException{
        zooKeeper = new ZooKeeper(hosts,SESSION_TIMEOUT,event->{
            if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                System.out.println("Connected to ZooKeeper");
            }
        });
    }

    public List<String> getRegisteredNodes(String path) throws KeeperException, InterruptedException{
        return  zooKeeper.getChildren(path,false);
    }
    public void close() throws InterruptedException {
        zooKeeper.close();
    }

    public static void main(String[] args) {
        ZookeeperClient zkClient = new ZookeeperClient();
        // 连接到 Zookeeper 集群的多个地址
        String hosts = "127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183"; // 这里列出集群中的所有节点
        String path = "/"; // 注册的服务路径，需替换为你实际的注册路径
        try {
            zkClient.connect(hosts);
            List<String> registeredNodes = zkClient.getRegisteredNodes(path);
            System.out.println("已注册的节点:");
            registeredNodes.forEach(System.out::println);


            // 关闭连接
            zkClient.close();

        }catch (IOException | KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }


}
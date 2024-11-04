package clark.zookeeperexercise;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

/**
 * ClassName: ZookeeperTest
 * Package: com.clark.zookeeperexercise
 */
public class ZookeeperTest {
    private static final int BASE_SLEEP_TIME = 1000;
    private static final int MAX_RETRIES = 3;
    public static CuratorFramework getConnect(){
        // Retry strategy. Retry 3 times, and will increase the sleep time between retries.
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES);
        CuratorFramework zkClient = CuratorFrameworkFactory.builder()
                // the server to connect to (can be a server list)
                .connectString("127.0.0.1:2181")
                .retryPolicy(retryPolicy)
                .build();
        zkClient.start();
        return zkClient;
    }

    public static void main(String[] args) throws Exception {
        CuratorFramework zkClient  = getConnect();
        //注意:下面的代码会报错，下文说了具体原因
//        zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/node1/00001");
        System.out.println("--------");
        System.out.println(zkClient.checkExists().forPath("/node1/00001"));//不为null的话，说明节点创建成功



    }


}

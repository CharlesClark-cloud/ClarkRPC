package clarkrpc.registry.zookeeper.utils;

import com.clarkrpc.config.ConfigReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * ClassName: CuratorUtil
 * Package: clarkrpc.registry.zookeeper.utils
    主要用于获得zk客户端 进行节点的增加和删除
 */
@Slf4j
public class CuratorUtil {
    //zk 客户端管理工具
    //所有的节点都存放在一个concurrentHahsMap中 这样查询效率更高
    private static final int BASE_SLEEP_TIME = 1000;  // 初始重试间隔时间（单位：毫秒）
    private static final int MAX_RETRIES = 3; // 最大重试次数

    public static final String ZK_REGISTER_ROOT_PATH = "/my-rpc"; // ZooKeeper 注册中心的根路径，所有服务都注册在此路径下

    private static final Map<String, List<String>> SERVICE_ADDRESS_MAP = new ConcurrentHashMap<>(); // <服务名称, 实例 IP 地址列表>的映射关系，用于缓存已注册服务的实例地址

    private static final Set<String> REGISTERED_PATH_SET = ConcurrentHashMap.newKeySet(); // 已注册的服务路径集合，避免重复注册

    private static CuratorFramework zkClient; // ZooKeeper 客户端，用于管理与 ZooKeeper 的连接和操作

    private static final String DEFAULT_ZOOKEEPER_ADDRESS = "127.0.0.1:2181"; // 默认的 ZooKeeper 地址（若未配置则使用此地址）
    private  static  ConfigReader configReader= new ConfigReader("application.properties");
    private CuratorUtil(){

    }

    public static CuratorFramework getZkClient(){
        // 配置文件是否设置zk地址 设置直接使用 否则使用默认的本机
        String zookeeperAddress = configReader.getZkAddress()!=null?configReader.getZkAddress():DEFAULT_ZOOKEEPER_ADDRESS;
        if(zkClient!=null && zkClient.getState() == CuratorFrameworkState.STARTED){
            //已经完成连接 直接返回zk 客户端对象
            return  zkClient;
        }
        //未连接过
        //设置重试策略
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES);
        zkClient = CuratorFrameworkFactory.builder()
                // 可以是集群
                .connectString(zookeeperAddress)
                .retryPolicy(retryPolicy)
                .build();
        zkClient.start();
        try {
            // wait 30s until connect to the zookeeper
            if (!zkClient.blockUntilConnected(30, TimeUnit.SECONDS)) {
                throw new RuntimeException("Time out waiting to connect to zookeeper!");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return zkClient;

    }
    //创建持久节点
    public  static void createPersistentNode(CuratorFramework zkClient,String path){
            try{
                if(REGISTERED_PATH_SET.contains(path)||zkClient.checkExists().forPath(path)!=null){
                    //已经有了此节点了
                    log.info("The node already exists. The node is:[{}]", path);
                }else {
                    //创建持久节点并添加到节点集合中
                    zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
                    REGISTERED_PATH_SET.add(path);
                    log.info("The node was created successfully. The node is:[{}]", path);
                }
            } catch (Exception e) {
                log.error("create persistent node for path [{}] fail", path);
            }

    }

    //监听注册服务中的节点变化
    private static void registerWatcher(String rpcServiceName, CuratorFramework zkClient) throws Exception {
        //确认服务实例路径
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
        //缓存信息
        PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient, servicePath, true);
        //监听节点是否变化
        PathChildrenCacheListener pathChildrenCacheListener = (curatorFramework, pathChildrenCacheEvent) -> {
            List<String> serviceAddresses = curatorFramework.getChildren().forPath(servicePath);
            //将此服务 以及其实例的地址都存入map中
            SERVICE_ADDRESS_MAP.put(rpcServiceName, serviceAddresses);
        };
        pathChildrenCache.getListenable().addListener(pathChildrenCacheListener);//添加监听器
        pathChildrenCache.start();//启动该服务的监听器
    }
    //获得一个服务路径的所有实例节点
    public static List<String> getChildrenNodes(CuratorFramework zkClient, String rpcServiceName) {
        if (SERVICE_ADDRESS_MAP.containsKey(rpcServiceName)) {
            return SERVICE_ADDRESS_MAP.get(rpcServiceName);
        }
        List<String> result = null;
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
        try {
            result = zkClient.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(rpcServiceName, result);
            registerWatcher(rpcServiceName, zkClient);
        } catch (Exception e) {
            log.error("get children nodes for path [{}] fail", servicePath);
        }
        return result;
    }
    //清空所有节点信息
    public static void clearRegistry(CuratorFramework zkClient, InetSocketAddress inetSocketAddress) {
        REGISTERED_PATH_SET.stream().parallel().forEach(p -> {
            //清楚节点集合信息
            try {
                if (p.endsWith(inetSocketAddress.toString())) {
                    //同时删除zk中的信息
                    zkClient.delete().forPath(p);
                }
            } catch (Exception e) {
                log.error("clear registry for path [{}] fail", p);
            }
        });
        log.info("All registered services on the server are cleared:[{}]", REGISTERED_PATH_SET.toString());
    }






}

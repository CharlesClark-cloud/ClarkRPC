package com.clarkrpc.loadbalance.loadbalancer;


import com.clarkrpc.loadbalance.AbstractLoadBalance;
import com.clarkrpc.remoting.dto.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * refer to dubbo consistent hash load balance: https://github.com/apache/dubbo/blob/2d9583adf26a2d8bd6fb646243a9fe80a77e65d5/dubbo-cluster/src/main/java/org/apache/dubbo/rpc/cluster/loadbalance/ConsistentHashLoadBalance.java
 *
 * @author RicardoZ
 * @createTime 2020年10月20日 18:15:20
 */
@Slf4j
public class ConsistentHashLoadBalance extends AbstractLoadBalance {
    private final ConcurrentHashMap<String, ConsistentHashSelector> selectors = new ConcurrentHashMap<>();
    //selectors：是一个 ConcurrentHashMap，存储了每个 RPC 服务名称对应的 ConsistentHashSelector 实例。
    // 每个 ConsistentHashSelector 负责一个服务的负载均衡。

    @Override
    protected String doSelect(List<String> serviceAddresses, RpcRequest rpcRequest) {
        //doSelect 方法负责根据 rpcRequest 选择一个具体的服务地址。
        //identityHashCode：用 System.identityHashCode 计算当前 serviceAddresses 的哈希码，用于检测服务地址列表是否发生变更。
        //rpcServiceName：由 rpcRequest 提取服务名称，用于标识服务。
        //selector：尝试从 selectors 缓存中获取 rpcServiceName 对应的 ConsistentHashSelector，如果没有找到，或地址列表发生变化，则创建新的 ConsistentHashSelector，并加入 selectors 缓存。
        //selector.select(rpcServiceName + Arrays.stream(rpcRequest.getParameters())) 将请求的服务名称和参数作为 key 进行哈希，从而选择一个合适的服务地址。
        int identityHashCode = System.identityHashCode(serviceAddresses);
        // build rpc service name by rpcRequest
        String rpcServiceName = rpcRequest.getRpcServiceName();
        ConsistentHashSelector selector = selectors.get(rpcServiceName);
        // check for updates
        if (selector == null || selector.identityHashCode != identityHashCode) {//判断服务提供地址集合是否更新 如果更新则缓存要更新
            selectors.put(rpcServiceName, new ConsistentHashSelector(serviceAddresses, 160, identityHashCode));
            selector = selectors.get(rpcServiceName);
        }
        return selector.select(rpcServiceName + Arrays.stream(rpcRequest.getParameters()));
    }

    static class ConsistentHashSelector {
        //每个服务第一次负载均衡时，缓存没有数据，一定会为每个服务生成n个虚拟节点，然后k-hash值：v-节点网络地址
        private final TreeMap<Long, String> virtualInvokers;//用来存储虚拟节点的哈希值和对应的服务地址。

        private final int identityHashCode;//用于表示服务地址列表的哈希值，用于检查服务地址列表是否发生变化。

        ConsistentHashSelector(List<String> invokers, int replicaNumber, int identityHashCode) {
            //replicaNumber表示每个服务实例生成的虚拟节点数，通常越多可以使节点分布更均匀。
            this.virtualInvokers = new TreeMap<>();
            this.identityHashCode = identityHashCode;

            for (String invoker : invokers) {
                for (int i = 0; i < replicaNumber / 4; i++) {
                    byte[] digest = md5(invoker + i);
                    for (int h = 0; h < 4; h++) {
                        long m = hash(digest, h);
                        virtualInvokers.put(m, invoker);
                        //virtualInvokers.put(m, invoker)：将生成的虚拟节点哈希值作为 key，
                        // 服务地址作为 value 存入 TreeMap，构建一致性哈希环。
                    }
                }
            }
        }

        static byte[] md5(String key) {
            //md5 方法对输入的 key 字符串进行 MD5 哈希计算，返回 128 位（16 字节）长度的哈希值 digest。
            // 用于计算虚拟节点位置。
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");
                byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
                md.update(bytes);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }

            return md.digest();
        }

        static long hash(byte[] digest, int idx) {
            //hash 方法用于从 digest 中提取部分字节组成一个 32 位无符号整数，作为虚拟节点在哈希环上的位置。
            return ((long) (digest[3 + idx * 4] & 255) << 24 | (long) (digest[2 + idx * 4] & 255) << 16 | (long) (digest[1 + idx * 4] & 255) << 8 | (long) (digest[idx * 4] & 255)) & 4294967295L;
        }

        public String select(String rpcServiceKey) { //根据调用的服务名称以及参数 生成的key
            byte[] digest = md5(rpcServiceKey); // 计算虚拟节点的位置 而虚拟节点有很多个
            return selectForKey(hash(digest, 0));//使用 selectForKey 方法找到离该位置最近的顺时针节点。
        }

        public String selectForKey(long hashCode) {
            Map.Entry<Long, String> entry = virtualInvokers.tailMap(hashCode, true).firstEntry();
//            selectForKey 方法通过 TreeMap 的 tailMap 方法找到第一个大于或等于 hashCode 的节点。
            if (entry == null) {
                entry = virtualInvokers.firstEntry();
            }

            return entry.getValue();
        }
    }
}

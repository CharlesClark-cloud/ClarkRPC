package com.clarkrpc.remoting.transport.netty.client;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用于缓存和获得channel对象
 * 是一个用于管理 Netty 客户端 Channel（连接）的工具类，主要负责缓存、获取和移除特定地址对应的 Channel 实例。
 * 其作用是为每个客户端的目标地址维护一个可复用的 Channel，从而减少重复创建连接的开销，提高网络通信效率。
 */
@Slf4j
public class ChannelProvider {

    //缓存的都放在map中
    private final Map<String, Channel> channelMap;

    public ChannelProvider() {
        channelMap = new ConcurrentHashMap<>();
    }

    public Channel get(InetSocketAddress inetSocketAddress) {
        String key = inetSocketAddress.toString();
        //确认是否已经有该地址的连接
        if (channelMap.containsKey(key)) {
            Channel channel = channelMap.get(key);
            //如果已经有了确认其是否可用
            if (channel != null && channel.isActive()) {
                //可用 直接返回
                return channel;
            } else {
                //否则移出缓存
                channelMap.remove(key);
            }
        }
        return null;
    }

    public void set(InetSocketAddress inetSocketAddress, Channel channel) {
        String key = inetSocketAddress.toString();
        channelMap.put(key, channel);
    }

    public void remove(InetSocketAddress inetSocketAddress) {
        String key = inetSocketAddress.toString();
        channelMap.remove(key);
        log.info("Channel map size :[{}]", channelMap.size());
    }
}

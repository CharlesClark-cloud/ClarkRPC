package com.clarkrpc.loadbalance;



import com.clarkrpc.remoting.dto.RpcRequest;
import com.clarkrpc.extension.SPI;

import java.util.List;

/**
 * Interface to the load balancing policy
 *
 * @author shuang.kou
 * @createTime 2020年06月21日 07:44:00
 */
@SPI
public interface LoadBalance {
    /**
     * Choose one from the list of existing service addresses list
     *
     * @param serviceUrlList Service address list
     * @param rpcRequest
     * @return target service address
     */
    String selectServiceAddress(List<String> serviceUrlList, RpcRequest rpcRequest);
}

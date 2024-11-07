package com.clarkrpc.remoting.transport;

import com.clarkrpc.remoting.dto.RpcRequest;
import com.clarkrpc.extension.SPI;

/**
 * ClassName: RpcRequestTransport
 * Package: clarkrpc.remoting
 */
//send RpcRequest
@SPI
public interface RpcRequestTransport {
    /**
     * 发送rpc request 到服务器 获得结果
     *
     * @param rpcRequest message body
     * @return data from server
     */
    Object sendRpcRequest(RpcRequest rpcRequest);
}

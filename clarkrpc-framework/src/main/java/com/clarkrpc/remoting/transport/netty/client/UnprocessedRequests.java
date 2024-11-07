package com.clarkrpc.remoting.transport.netty.client;


import com.clarkrpc.remoting.dto.RpcResponse;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 这个类 UnprocessedRequests 的作用是管理客户端发送的异步请求及其对应的未处理响应，
 * 以便在响应返回时将结果与对应的请求进行匹配。这在异步 RPC 调用中非常常见，因为请求和响应在网络上是独立发送和接收的。
 */
public class UnprocessedRequests {
    private static final Map<String, CompletableFuture<RpcResponse<Object>>> UNPROCESSED_RESPONSE_FUTURES = new ConcurrentHashMap<>();
    /*
    * CompletableFuture<RpcResponse<Object>> 用于在客户端等待 RPC 调用的异步响应。
    * 客户端发出请求后，可以得到一个 CompletableFuture，而不必阻塞等待响应。
    * 相应地，响应到达后可以直接完成这个 CompletableFuture，通知调用方任务完成。*/
    public void put(String requestId, CompletableFuture<RpcResponse<Object>> future) {
        UNPROCESSED_RESPONSE_FUTURES.put(requestId, future);
    }

    public void complete(RpcResponse<Object> rpcResponse) {
        CompletableFuture<RpcResponse<Object>> future = UNPROCESSED_RESPONSE_FUTURES.remove(rpcResponse.getRequestId());
        if (null != future) {
//          如果找到了 CompletableFuture 对象，则将 RpcResponse 传给 future.complete()，
//          以完成该 CompletableFuture，通知客户端响应已返回。
//	        如果找不到 CompletableFuture（即 future 为 null），
//	        说明没有与该请求匹配的 CompletableFuture，可能是因为超时或其他异常情况，因此抛出 IllegalStateException 异常。
            future.complete(rpcResponse);
        } else {
            throw new IllegalStateException();
        }
    }
}

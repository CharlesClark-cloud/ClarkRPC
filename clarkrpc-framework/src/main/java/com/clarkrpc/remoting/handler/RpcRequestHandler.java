package com.clarkrpc.remoting.handler;

import com.clarkrpc.provider.ServiceProvider;
import com.clarkrpc.provider.impl.ZkServiceProviderImpl;
import com.clarkrpc.remoting.dto.RpcRequest;
import com.clarkrpc.exception.RpcException;
import com.clarkrpc.factory.SingletonFactory;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * ClassName: RpcRequestHandler
 * Package: clarkrpc.remoting.handler
 */

//此类是对客户端发送的rpc请求进行处理
@Slf4j
public class RpcRequestHandler {
    private final ServiceProvider serviceProvider;
    public RpcRequestHandler() {
        serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);
    }
    //调用响应的方法，并返回该方法
    public Object handle(RpcRequest rpcRequest){
        Object service = serviceProvider.getService(rpcRequest.getRpcServiceName());
        return  invokeTargetMethod(rpcRequest,service);
    }
    /**
     * get method execution results
     * 获得方法的执行结果
     * @param rpcRequest client request
     * @param service    service object
     * @return the result of the target method execution 将执行结果封装到object 返回
     */
    public  Object invokeTargetMethod(RpcRequest rpcRequest,Object service){
        Object result;
        try {
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(),rpcRequest.getParamTypes());
            result = method.invoke(service, rpcRequest.getParameters());
            log.info("service:[{}] successful invoke method:[{}]", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        } catch (NoSuchMethodException | IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
            throw new RpcException(e.getMessage(), e);
        }
        return result;
    }

}

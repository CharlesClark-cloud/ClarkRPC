package com.clarkrpc.remoting.transport.netty.server;

import com.clarkrpc.remoting.constants.RpcConstants;
import com.clarkrpc.remoting.dto.RpcMessage;
import com.clarkrpc.remoting.dto.RpcRequest;
import com.clarkrpc.remoting.dto.RpcResponse;
import com.clarkrpc.remoting.handler.RpcRequestHandler;
import com.clarkrpc.enums.CompressTypeEnum;
import com.clarkrpc.enums.RpcResponseCodeEnum;
import com.clarkrpc.enums.SerializationTypeEnum;
import com.clarkrpc.factory.SingletonFactory;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;

/**
 * ClassName: NettyRpcServerHandler
 * Package: clarkrpc.remoting.transport.netty.server
 * 自定义 一个netty 的 channelHandler  来处理客户端发来的数据

 * <p>
 * 如果继承自 SimpleChannelInboundHandler 的话就不要考虑 ByteBuf 的释放 ，{@link SimpleChannelInboundHandler} 内部的
 * channelRead 方法会替你释放 ByteBuf ，避免可能导致的内存泄露问题。详见《Netty进阶之路 跟着案例学 Netty》

 *
  */
@Slf4j
public class NettyRpcServerHandler extends ChannelInboundHandlerAdapter {

    private  final RpcRequestHandler rpcRequestHandler;
    public NettyRpcServerHandler(){
        this.rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg instanceof RpcMessage) {
                //确认是rpc消息
                log.info("server receive msg: [{}] ", msg);
                byte messageType = ((RpcMessage) msg).getMessageType();//获取消息类型
                RpcMessage rpcMessage = new RpcMessage();//定义一个新消息 用户传输
                rpcMessage.setCodec(SerializationTypeEnum.HESSIAN.getCode());//设置序列化类型
                rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());//设置压缩类型
                if (messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE) {//心跳请求
                    rpcMessage.setMessageType(RpcConstants.HEARTBEAT_RESPONSE_TYPE);//设置返回信息为 心跳响应
                    rpcMessage.setData(RpcConstants.PONG);//设置返回信息内容为pong
                } else {//为方法调用请求信息
                    RpcRequest rpcRequest = (RpcRequest) ((RpcMessage) msg).getData();//获得rpc消息体 转为rpc request
                    // Execute the target method (the method the client needs to execute) and return the method result
                    Object result = rpcRequestHandler.handle(rpcRequest); //执行rpcrequesthandler 中的处理请求方法，返回方法调用结果
                    log.info(String.format("server get result: %s", result.toString()));//方法调用结果
                    rpcMessage.setMessageType(RpcConstants.RESPONSE_TYPE); //设置返回消息类型为 响应消息
                    if (ctx.channel().isActive() && ctx.channel().isWritable()) {
                        //如果连接还是活跃状态，且通道可以传入信息
                        RpcResponse<Object> rpcResponse = RpcResponse.success(result, rpcRequest.getRequestId());//将结果与请求id封装返回
                        rpcMessage.setData(rpcResponse);//将响应消息放入消息体中
                    } else {
                        //如果通道关闭，或者通道不可写 那么消息丢失
                        RpcResponse<Object> rpcResponse = RpcResponse.fail(RpcResponseCodeEnum.FAIL);
                        rpcMessage.setData(rpcResponse);
                        log.error("not writable now, message dropped");
                    }
                }
                //waf将 rpcMessage 通过 Netty 的管道写入并立即刷新发送到客户端，
                // add这个方法给写操作添加了一个监听器，如果写操作失败，则立即关闭该 Channel。
                //这样设计的目的是提升系统的容错性和资源释放：当发生写入失败时，立即关闭无效连接，避免资源浪费。
                ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } finally {
           //Ensure that ByteBuf is released, otherwise there may be memory leaks
           //要释放缓冲区否则可能内存泄露
           ReferenceCountUtil.release(msg);
       }
    }
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                log.info("idle check happen, so close the connection");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("server catch exception");
        cause.printStackTrace();
        ctx.close();
    }
}

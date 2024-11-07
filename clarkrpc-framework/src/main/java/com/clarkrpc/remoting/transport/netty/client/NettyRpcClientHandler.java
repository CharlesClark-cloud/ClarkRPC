package com.clarkrpc.remoting.transport.netty.client;

import com.clarkrpc.remoting.constants.RpcConstants;
import com.clarkrpc.remoting.dto.RpcMessage;
import com.clarkrpc.remoting.dto.RpcResponse;
import com.clarkrpc.enums.CompressTypeEnum;
import com.clarkrpc.enums.SerializationTypeEnum;
import com.clarkrpc.factory.SingletonFactory;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * ClassName: NettyClientHandler
 * Package: clarkrpc.remoting.transport.netty.client
 *自定义客户端通道处理器来处理由客户端返回的消息
 * <p>
 * 如果继承自 SimpleChannelInboundHandler 的话就不要考虑 ByteBuf 的释放 ，{@link SimpleChannelInboundHandler} 内部的
 * channelRead 方法会替你释放 ByteBuf ，避免可能导致的内存泄露问题。详见《Netty进阶之路 跟着案例学 Netty》
 */
@Slf4j
public class NettyRpcClientHandler extends ChannelInboundHandlerAdapter {
    private final UnprocessedRequests unprocessedRequests; //未处理的请求
    private final NettyRpcClient nettyRpcClient;

    public NettyRpcClientHandler() {
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        this.nettyRpcClient = SingletonFactory.getInstance(NettyRpcClient.class);
    }

    /**
     *读取服务器发送的消息
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            log.info("client receive msg: [{}]", msg);
            if (msg instanceof RpcMessage) {
                //消息是rpc 消息
                RpcMessage tmp = (RpcMessage) msg;
                byte messageType = tmp.getMessageType();//判断消息类型
                if (messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
                    //心跳响应
                    log.info("heart [{}]", tmp.getData());
                } else if (messageType == RpcConstants.RESPONSE_TYPE) {
                    //消息响应
                    RpcResponse<Object> rpcResponse = (RpcResponse<Object>) tmp.getData();//获得消息体 转换为rpcresponse类型
                    unprocessedRequests.complete(rpcResponse);
                }
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                log.info("write idle happen [{}]", ctx.channel().remoteAddress());
                Channel channel = nettyRpcClient.getChannel((InetSocketAddress) ctx.channel().remoteAddress());
                RpcMessage rpcMessage = new RpcMessage();
                rpcMessage.setCodec(SerializationTypeEnum.PROTOSTUFF.getCode());
                rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
                rpcMessage.setMessageType(RpcConstants.HEARTBEAT_REQUEST_TYPE);
                rpcMessage.setData(RpcConstants.PING);
                channel.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    /**
     * Called when an exception occurs in processing a client message
     * 异常提醒
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("client catch exception：", cause);
        cause.printStackTrace();
        ctx.close();
    }
}

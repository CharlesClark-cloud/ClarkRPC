package com.clarkrpc.remoting.transport.netty.server;

/**
 * ClassName: NettyRpcServer
 * Package: clarkrpc.remoting.transport.netty.server
 */

import com.clarkrpc.config.CustomShutdownHook;
import com.clarkrpc.config.RpcServiceConfig;
import com.clarkrpc.provider.ServiceProvider;
import com.clarkrpc.provider.impl.ZkServiceProviderImpl;
import com.clarkrpc.remoting.transport.netty.codec.RpcMessageDecoder;
import com.clarkrpc.remoting.transport.netty.codec.RpcMessageEncoder;
import com.clarkrpc.factory.SingletonFactory;
import com.clarkrpc.utils.RuntimeUtil;
import com.clarkrpc.utils.concurrent.threadpool.ThreadPoolFactoryUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

/**
 * Server. Receive the client message, call the corresponding method according to the client message,
 * and then return the result to the client.
 *服务器接受到客户端的消息，然后调用对应的方法并返回结果给客户端
 */
@Slf4j
@Component
public class NettyRpcServer {

    public static final int PORT = 9998;

    private final ServiceProvider serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);

    public void registerService(RpcServiceConfig rpcServiceConfig) {
        serviceProvider.publishService(rpcServiceConfig);
    }

    @SneakyThrows
    public void start() {
        CustomShutdownHook.getCustomShutdownHook().clearAll();
        String host = InetAddress.getLocalHost().getHostAddress();
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        DefaultEventExecutorGroup serviceHandlerGroup = new DefaultEventExecutorGroup(
                RuntimeUtil.cpus() * 2,
                ThreadPoolFactoryUtil.createThreadFactory("service-handler-group", false)
        );
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    // TCP默认开启了 Nagle 算法，该算法的作用是尽可能的发送大数据快，减少网络传输。TCP_NODELAY 参数的作用就是控制是否启用 Nagle 算法。
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    // 是否开启 TCP 底层心跳机制
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    //表示系统用于临时存放已完成三次握手的请求的队列的最大长度,如果连接建立频繁，服务器处理创建新连接较慢，可以适当调大这个参数
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    // 当客户端第一次进行请求的时候才会进行初始化
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            // 30 秒之内没有收到客户端请求的话就关闭连接
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
                            p.addLast(new RpcMessageEncoder());
                            p.addLast(new RpcMessageDecoder());
                            p.addLast(serviceHandlerGroup, new NettyRpcServerHandler());
                        }
                    });

            // 绑定端口，同步等待绑定成功
            ChannelFuture f = b.bind(host, PORT).sync();
            // 等待服务端监听端口关闭
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("occur exception when start server:", e);
        } finally {
            log.error("shutdown bossGroup and workerGroup");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            serviceHandlerGroup.shutdownGracefully();
        }
    }


}
//@Slf4j
//@Component
//public class NettyRpcServer {
//    public static final int PORT = 9998;
//    private  final ServiceProvider serviceProvider = SingletonFactory.getInstance(ServiceProvider.class);
//    public void registerService(RpcServiceConfig rpcServiceConfig){
//        //注册服务，也就是发布服务
//        serviceProvider.publishService(rpcServiceConfig);
//    }
//    //对下面的注解进行解释，隐式抛出异常注解，简化异常处理：
//    //	1•	简化异常处理：当使用 @SneakyThrows 注解时，Lombok 会在编译期间自动为你生成异常处理代码。
//    //	2.	隐式抛出异常：在运行时被隐式地抛出，而不需要你明确声明它们。
//    @SneakyThrows
//    public  void  start(){
//        //这个 start() 方法实现了一个 Netty RPC 服务器的启动过程，
//        // 从设置清理钩子、初始化网络组件、绑定端口到等待关闭，完整地展现了一个网络服务的基本架构。
//        // 它在设计上考虑到了高并发、稳定性和资源管理等方面，是构建高效 RPC 框架的重要组成部分。
//
//        //添加一个关闭钩子，确保在 JVM 关闭时执行必要的清理操作，比如注册中心的注销等。确保在服务器关闭时能正确处理资源释放。
//        CustomShutdownHook.getCustomShutdownHook().clearAll();
//        //获取rpc框架服务器的ip将在后面的绑定过程中使用
//        String host = InetAddress.getLocalHost().getHostAddress();
//        //bossGroup：负责接收客户端连接的线程组，通常只需要一个线程。
//        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
//        //workerGroup：负责处理已接受连接的线程组，数量可以根据需要动态调整（默认使用 CPU 核心数的两倍）。
//        EventLoopGroup workerGroup = new NioEventLoopGroup();
//        //serviceHandlerGroup：一个独立的线程池，专门用于处理 RPC 服务的业务逻辑。这里根据 CPU 核心数创建了两倍数量的线程来处理请求，提高并发处理能力。
//        DefaultEventExecutorGroup serviceHandlerGroup = new DefaultEventExecutorGroup(
//                RuntimeUtil.cpus()*2,
//                ThreadPoolFactoryUtil.createThreadFactory("service-handler-group", false)
//        );
//        try {
//            ServerBootstrap b = new ServerBootstrap();//用于启动和配置 Netty 服务器的核心类。
//            b.group(bossGroup, workerGroup)//将之前创建的 bossGroup 和 workerGroup 关联到这个服务器。
//                    .channel(NioServerSocketChannel.class)//指定使用 NIO 的服务器套接字通道
//                    // TCP默认开启了 Nagle 算法，该算法的作用是尽可能的发送大数据快，减少网络传输。TCP_NODELAY 参数的作用就是控制是否启用 Nagle 算法。
//                    .childOption(ChannelOption.TCP_NODELAY, true)
//                    // 是否开启 TCP 底层心跳机制
//                    .childOption(ChannelOption.SO_KEEPALIVE, true)
//                    //表示系统用于临时存放已完成三次握手的请求的队列的最大长度,如果连接建立频繁，服务器处理创建新连接较慢，可以适当调大这个参数
//                    .option(ChannelOption.SO_BACKLOG, 128)
//                    .handler(new LoggingHandler(LogLevel.INFO))
//                    // 当客户端第一次进行请求的时候才会进行初始化
//                    .childHandler(new ChannelInitializer<SocketChannel>() {
//                        @Override
//                        protected void initChannel(SocketChannel ch) {
//                            // 30 秒之内没有收到客户端请求的话就关闭连接
//                            ChannelPipeline p = ch.pipeline();//初始化每个新连接的管道，添加各种处理器：
//                            p.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));//如果 30 秒内没有接收到客户端请求，关闭连接。
//                            p.addLast(new RpcMessageEncoder());//用于编码 RPC 消息。
//                            p.addLast(new RpcMessageDecoder());//用于解码 RPC 消息。
//                            p.addLast(serviceHandlerGroup, new NettyRpcServerHandler());//处理具体的 RPC 业务逻辑。
//                        }
//                    });
//
//            // 绑定端口，同步等待绑定成功
//            ChannelFuture f = b.bind(host, PORT).sync();//绑定端口：将服务器绑定到指定的 IP 地址和端口号，并同步等待绑定完成。
//            // 等待服务端监听端口关闭
//            f.channel().closeFuture().sync();//等待关闭：在此调用将阻塞，直到服务器的通道关闭，这意味着服务器正在运行。
//        } catch (InterruptedException e) {
//            log.error("occur exception when start server:", e);
//        } finally {
//            //在 finally 块中确保所有线程组和服务处理器组被优雅地关闭，释放相关资源。这是防止资源泄露的重要步骤。
//            log.error("shutdown bossGroup and workerGroup");
//            bossGroup.shutdownGracefully();
//            workerGroup.shutdownGracefully();
//            serviceHandlerGroup.shutdownGracefully();
//        }
//    }
//
//}

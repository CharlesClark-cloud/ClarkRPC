package com.clarkrpc.remoting.constants;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

//一个存放RPC框架中常用的常量的类
public class RpcConstants {
    //其中包括自定义网络传输协议的定义中用到常量

    //魔法数 验证rpc message
    public static final byte[] MAGIC_NUMBER = {(byte) 'g', (byte) 'r', (byte) 'p', (byte) 'c'};
    //默认字符集
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    //version information 版本信息
    public static final byte VERSION = 1;
    //总长
    public static final byte TOTAL_LENGTH = 16;
    public static final byte REQUEST_TYPE = 1; //请求类型
    public static final byte RESPONSE_TYPE = 2;//响应类型
    //ping
    public static final byte HEARTBEAT_REQUEST_TYPE = 3; //心跳请求类型
    //pong
    public static final byte HEARTBEAT_RESPONSE_TYPE = 4; //心跳响应类型
    public static final int HEAD_LENGTH = 16; //信息头 长度
    public static final String PING = "ping";
    public static final String PONG = "pong";
    public static final int MAX_FRAME_LENGTH = 8 * 1024 * 1024; //最大长度

}

package com.clarkrpc.remoting.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcMessage {

    /**
     * rpc message type 信息类型
     */
    private byte messageType;
    /**
     * serialization type  序列化类型
     */
    private byte codec;
    /**
     * compress type 压缩类型
     */
    private byte compress;
    /**
     * request id 请求id，响应时把信息发给对应id
     */
    private int requestId;
    /**
     * request data 数据
     */
    private Object data;

}

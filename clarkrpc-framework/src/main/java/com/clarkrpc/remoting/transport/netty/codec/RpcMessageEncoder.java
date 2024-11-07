package com.clarkrpc.remoting.transport.netty.codec;

/**
 * ClassName: RpcMessageEncoder
 * Package: clarkrpc.remoting.transport.netty.codec
 */


import com.clarkrpc.remoting.constants.RpcConstants;
import com.clarkrpc.remoting.dto.RpcMessage;
import com.clarkrpc.serialize.Serializer;
import com.clarkrpc.compress.Compress;
import com.clarkrpc.enums.CompressTypeEnum;
import com.clarkrpc.enums.SerializationTypeEnum;
import com.clarkrpc.extension.ExtensionLoader;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * custom protocol decoder  用户协议编码
 * <p>
 * <pre>
 *   0     1     2     3     4        5     6     7     8         9          10      11     12  13  14   15 16
 *   +-----+-----+-----+-----+--------+----+----+----+------+-----------+-------+----- --+-----+-----+-------+
 *   |   magic   code        |version | full length         | messageType| codec|compress|    RequestId       |
 *   +-----------------------+--------+---------------------+-----------+-----------+-----------+------------+
 *   |                                                                                                       |
 *   |                                         body                                                          |
 *   |                                                                                                       |
 *   |                                        ... ...                                                        |
 *   +-------------------------------------------------------------------------------------------------------+
 * 4B  magic code（魔法数）   1B version（版本）   4B full length（消息长度）    1B messageType（消息类型）
 * 1B compress（压缩类型） 1B codec（序列化类型）    4B  requestId（请求的Id）
 * body（object类型数据）
 */
@Slf4j
public class RpcMessageEncoder extends MessageToByteEncoder<RpcMessage> {
    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);
    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMessage rpcMessage, ByteBuf out) throws Exception {
            try {
                out.writeBytes(RpcConstants.MAGIC_NUMBER);
                out.writeByte(RpcConstants.VERSION);
                // 预留位置给 全长
                out.writerIndex(out.writerIndex() + 4);
                byte messageType = rpcMessage.getMessageType();
                out.writeByte(messageType);
                out.writeByte(rpcMessage.getCodec());
                out.writeByte(CompressTypeEnum.GZIP.getCode());
                out.writeInt(ATOMIC_INTEGER.getAndIncrement());
                // build full length
                byte[] bodyBytes = null;
                int fullLength = RpcConstants.HEAD_LENGTH;//初始化为消息头长度
                // if messageType is not heartbeat message,fullLength = head length + body length
                //如果是非心跳信息，全长等于头长度加消息体长度
                if (messageType != RpcConstants.HEARTBEAT_REQUEST_TYPE
                        && messageType != RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
                    // serialize the object
                    String codecName = SerializationTypeEnum.getName(rpcMessage.getCodec());
                    log.info("codec name: [{}] ", codecName);//在日志中输出序列化后的数据类型
                    Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class)
                            .getExtension(codecName);
                    bodyBytes = serializer.serialize(rpcMessage.getData());
                    // compress the bytes
                    String compressName = CompressTypeEnum.getName(rpcMessage.getCompress());
                    Compress compress = ExtensionLoader.getExtensionLoader(Compress.class)
                            .getExtension(compressName);
                    bodyBytes = compress.compress(bodyBytes);
                    fullLength += bodyBytes.length; //对消息总长进行修改
                }

                if (bodyBytes != null) {
                    out.writeBytes(bodyBytes);
                }
                int writeIndex = out.writerIndex();
                out.writerIndex(writeIndex - fullLength + RpcConstants.MAGIC_NUMBER.length + 1);
                out.writeInt(fullLength);//写入总长
                out.writerIndex(writeIndex);

            }catch (Exception e){
                log.error("Encode request error!", e);
            }
    }
}

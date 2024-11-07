package com.clarkrpc.serialize.kryo;

import com.clarkrpc.remoting.dto.RpcRequest;
import com.clarkrpc.remoting.dto.RpcResponse;
import com.clarkrpc.serialize.Serializer;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * ClassName: KryoSerializer
 * Package: clarkrpc.serialize.ktyo
 */
@Slf4j
public class KryoSerializer  implements Serializer {
    //序列器 序列化效率高，但是缺点是只兼容java
    /**
     * Because Kryo is not thread safe. So, use ThreadLocal to store Kryo objects
     * 因为kryo 是线程不安全的，所以需要使用ThreadLocal 来存储kryo对象
     */
    private  final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(()->{
        Kryo kryo = new Kryo();
        kryo.register(RpcRequest.class);
        kryo.register(RpcResponse.class);
        return kryo;
    });

    @Override
    public byte[] serialize(Object obj) {
        try(ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                Output output = new Output(byteArrayOutputStream)){
            // Object->byte:将对象序列化为byte数组
            Kryo kryo = kryoThreadLocal.get();
            kryo.writeObject(output,obj);
            output.flush();
            return byteArrayOutputStream.toByteArray();

        } catch (Exception e) {
            log.error("Serialization failed", e);
            throw new SerializeException("Serialization failed", e);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            Input input = new Input(byteArrayInputStream)){
            Kryo kryo = kryoThreadLocal.get();
            // byte->Object:从byte数组中反序列化出对对象
            T t = kryo.readObject(input, clazz);
            return t;
        }
        catch (Exception e) {
            log.error("Deserialization failed", e);
            throw new SerializeException("Deserialization failed", e);
        }

    }

    //内部异常类
    public class SerializeException extends RuntimeException {
        public SerializeException(String message) {
            super(message);
        }

        public SerializeException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

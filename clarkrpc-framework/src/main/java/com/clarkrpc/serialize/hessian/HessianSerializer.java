package com.clarkrpc.serialize.hessian;

import com.clarkrpc.serialize.Serializer;
import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import com.clarkrpc.exception.SerializeException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * ClassName: HessianSerializer
 * Package: clarkrpc.serialize.hessian
 */
public class HessianSerializer implements Serializer {
    //Hessian是一种动态类型、二进制序列化和Web服务协议，专为面向对象传输而设计。
    @Override
    public byte[] serialize(Object obj) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            HessianOutput hessianOutput = new HessianOutput(byteArrayOutputStream);
            hessianOutput.writeObject(obj);

            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            throw new SerializeException("Serialization failed");
        }

    }
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {

        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes)) {
            HessianInput hessianInput = new HessianInput(byteArrayInputStream);
            Object o = hessianInput.readObject();

            return clazz.cast(o);

        } catch (Exception e) {
            throw new SerializeException("Deserialization failed");
        }

    }
}

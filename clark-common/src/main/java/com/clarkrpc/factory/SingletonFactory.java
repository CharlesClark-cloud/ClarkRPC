package com.clarkrpc.factory;



import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ClassName: SingletonFactory
 * Package: com.clarkrpc.factory
 */
public class SingletonFactory {
    //单例工厂 单例模式可以避免很多问题
    public  static  final  Map<String, Object> OBJECT_MAP = new ConcurrentHashMap<>();
    public  static  final Object lock = new Object();
    private SingletonFactory(){

    }
    public static <T> T getInstance(Class<T> c){
        if (c == null) {
            throw new IllegalArgumentException();
        }
        String key = c.toString();
        if(OBJECT_MAP.containsKey(key)){
            return c.cast(OBJECT_MAP.get(key));
        }else {
            synchronized (lock){
                if(!OBJECT_MAP.containsKey(key)){
                    //双检锁
                    try {
                        T instance = c.getDeclaredConstructor().newInstance();
                        OBJECT_MAP.put(key,instance);
                        return  instance;
                    } catch (InvocationTargetException |InstantiationException |IllegalAccessException |NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }

                }else {
                    return c.cast(OBJECT_MAP.get(key));
                }
            }
        }
    }
}

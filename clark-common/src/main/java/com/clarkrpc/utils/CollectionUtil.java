package com.clarkrpc.utils;

import java.util.Collection;

public class CollectionUtil {

    //判断集合是否为空
    public static boolean isEmpty(Collection<?> c) {
        return c == null || c.isEmpty();
    }

}

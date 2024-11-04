package clark.dynamicproxyexercise.cglib;

import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.MethodProxy;

/**
 * ClassName: MethodInterceptor
 * Package: com.clark.dynamicproxyexercise.cglib
 */
public interface MethodInterceptor
        extends Callback {
    // 拦截被代理类中的方法
    public Object intercept(Object obj, java.lang.reflect.Method method, Object[] args,
                            MethodProxy proxy) throws Throwable;
}

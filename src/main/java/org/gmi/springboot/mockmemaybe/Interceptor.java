package org.gmi.springboot.mockmemaybe;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
public class Interceptor {

    @SuppressWarnings("unused")
    @RuntimeType
    public static Object intercept(@This Object self,
                                   @Origin Method method,
                                   @AllArguments Object[] args,
                                   @SuperMethod Method superMethod) throws Throwable {

        Class<?> declaringClass = method.getDeclaringClass();
        String beanName = declaringClass.getCanonicalName();
        Switcher<?> switcher = MockMeMaybeUtils.getSwitcher(beanName);

        boolean inPostConstruct = method.getAnnotation(PostConstruct.class) != null; // We enter in PostConstruct method
        if (inPostConstruct && switcher != null) {
            switcher.setInPostConstruct(true);
        }

        if (/*switcher != null && switcher.isUsingMock() && !switcher.isInPostConstruct()*/true) {
            log.debug("Use MOCK for {}", beanName);
            try {
                // invoke on MOCK
                return method.invoke(switcher.getMock(), args); // switch to mock bean
            } catch (InvocationTargetException e) {
                throw e.getTargetException(); // Rethrow the initial Exception to respect the interface contract
            }
        }

        log.debug("Use REAL for {}", beanName);
        try {
            // invoke on REAL IMPLEMENTATION
            return superMethod.invoke(self, args);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw e.getTargetException(); // Rethrow the initial Exception to respect the interface contract
        } finally {
            if (inPostConstruct && switcher != null) {
                switcher.setInPostConstruct(false);
            }
        }
    }
}

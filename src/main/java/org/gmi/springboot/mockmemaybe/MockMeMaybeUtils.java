package org.gmi.springboot.mockmemaybe;

import org.mockito.Answers;
import org.mockito.MockSettings;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class MockMeMaybeUtils {
    private static final Map<String, Switcher<?>> SWITCHERS = new HashMap<>();

    public static <T> void installSwitcher(Class<T> clazz) {
        SWITCHERS.put(clazz.getCanonicalName(), new Switcher<T>());
    }
    public static void backToRealImplementations() {
        for (Switcher<?> switcher : SWITCHERS.values()) {
            switcher.setUsingMock(false);
            if(switcher.getMock() != null){
                Mockito.reset(switcher.getMock());
            }
        }
    }

    public static <T> void  backToRealImplementation(Class<? extends T> clazz) {
        String beanName = clazz.getCanonicalName();
        if (SWITCHERS.containsKey(beanName)) {
            @SuppressWarnings("unchecked")
            Switcher<T> switcher = (Switcher<T>) SWITCHERS.get(beanName);

            switcher.setUsingMock(false);
            if(switcher.getMock() != null){
                 Mockito.reset(switcher.getMock());
             }
            return;
        }
        throw new RuntimeException(String.format("Want's to use mock '%s' but not registered", clazz.getCanonicalName()));

    }

    public static <T> T gimmeMock(Class<? extends T> clazz) {
        String beanName = clazz.getCanonicalName();
        if (SWITCHERS.containsKey(beanName)) {
            @SuppressWarnings("unchecked")
            Switcher<T> switcher = (Switcher<T>) SWITCHERS.get(beanName);
            switcher.setMock(mock(clazz, Answers.RETURNS_SMART_NULLS));
            switcher.setUsingMock(true);
            return switcher.getMock();
        }
        throw new RuntimeException(String.format("Want's to use mock '%s' but not registered", clazz.getCanonicalName()));
    }
    public static <T> T gimmeSpy(Class<? extends T> clazz, T bean) {
        String beanName = clazz.getCanonicalName();
        if (SWITCHERS.containsKey(beanName)) {
            @SuppressWarnings("unchecked")
            Switcher<T> switcher = (Switcher<T>) SWITCHERS.get(beanName);

            switcher.setMock(spy(bean));
            switcher.setUsingMock(true);
            return switcher.getMock();
        }
        throw new RuntimeException(String.format("Want's to use mock '%s' but not registered", clazz.getCanonicalName()));
    }

    public static Switcher<?> getSwitcher(String beanName) {
        return SWITCHERS.get(beanName);
    }
}

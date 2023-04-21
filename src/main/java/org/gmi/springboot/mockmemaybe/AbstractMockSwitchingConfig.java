package org.gmi.springboot.mockmemaybe;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Configuration;

import static org.gmi.springboot.mockmemaybe.MockMeMaybeUtils.installSwitcher;

@Configuration
public abstract class AbstractMockSwitchingConfig implements BeanDefinitionRegistryPostProcessor {

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        registerAllSwitchers(registry);
    }

    protected abstract void registerAllSwitchers(BeanDefinitionRegistry registry);

    protected static <T> void mockMeMaybe(BeanDefinitionRegistry registry, Class<T> clazz, String... initialBeanName) {

        if(clazz.isInterface()){
            throw new RuntimeException("Not yet implemented");
        }

        installSwitcher(clazz);

        // Create an Interceptor class wrapping the service
        Class<?> type = new ByteBuddy()
          .subclass(clazz, ConstructorStrategy.Default.IMITATE_SUPER_CLASS)
          .method(ElementMatchers.any()).intercept(MethodDelegation.to(Interceptor.class))
          .make()
          .load(clazz.getClassLoader())
          .getLoaded();

        AnnotatedGenericBeanDefinition beanDefinition = new AnnotatedGenericBeanDefinition(type);
        beanDefinition.setPrimary(true);

        String beanName = getBeanName(clazz, initialBeanName);
        if(registry.containsBeanDefinition(beanName)) {
            registry.removeBeanDefinition(beanName);
        }
        registry.registerBeanDefinition(beanName, beanDefinition);
    }

    private static <T> String getBeanName(Class<T> clazz, String... initialBeanName) {
        String beanName = clazz.getSimpleName().substring(0,1).toLowerCase() + clazz.getSimpleName().substring(1);

        if(initialBeanName.length != 0){
            beanName = initialBeanName[0];
        }
        return beanName;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }
}

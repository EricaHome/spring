package framework;

public interface LuBanBeanPostProcessor {

    void postProcessBeforeInitialization(String beanName, Object bean);
    void postProcessAfterInitialization(String beanName, Object bean);

}

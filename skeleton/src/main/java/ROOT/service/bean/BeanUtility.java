package {{rootPackage}}.service.bean;

import {{rootPackage}}.util.Check;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

/**
 * Bean lookup for code Spring does not instantiate, such as JPA entity listeners.
 */
@Service
public final class BeanUtility implements ApplicationContextAware {

    private static ApplicationContext context;

    /**
     * @param clazz bean type
     * @param <T> bean type
     * @return the bean
     */
    public static <T> T getBean(final Class<T> clazz) {
        Check.isNotNull(clazz, "clazz");
        return context.getBean(clazz);
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) {
        Check.isNotNull(applicationContext, "applicationContext");
        setContext(applicationContext);
    }

    private static void setContext(final ApplicationContext applicationContext) {
        context = applicationContext;
    }
}

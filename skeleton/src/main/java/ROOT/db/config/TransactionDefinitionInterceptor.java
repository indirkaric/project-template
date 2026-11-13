package {{rootPackage}}.db.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.NamedThreadLocal;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.interceptor.TransactionAttributeSource;

/**
 * Remembers the outermost transaction definition so {@link DataSourceRouting} can pick a pool.
 * Ordered before Spring's TransactionInterceptor, because the connection is fetched when the
 * transaction begins.
 */
@Aspect
@Component
public final class TransactionDefinitionInterceptor implements Ordered {

    private static final int ORDER_OFFSET = 10;

    private static final ThreadLocal<TransactionDefinition> CURRENT =
            new NamedThreadLocal<>("Current aspect-driven transaction definition");

    private final TransactionAttributeSource transactionAttributeSource;

    /**
     * @param transactionAttributeSource Spring's transaction attribute source
     */
    public TransactionDefinitionInterceptor(final TransactionAttributeSource transactionAttributeSource) {
        this.transactionAttributeSource = transactionAttributeSource;
    }

    /**
     * @return true inside a transaction that is not read-only
     */
    public static boolean isCurrentTransactionReadWrite() {
        TransactionDefinition definition = CURRENT.get();
        return definition != null && !definition.isReadOnly();
    }

    /**
     * @param joinPoint intercepted call
     * @return its result
     * @throws Throwable whatever the call throws
     */
    @Around("@annotation(org.springframework.transaction.annotation.Transactional)"
            + " || @within(org.springframework.transaction.annotation.Transactional)")
    public Object rememberTransactionDefinition(final ProceedingJoinPoint joinPoint) throws Throwable {
        boolean outermost = CURRENT.get() == null;
        if (outermost) {
            CURRENT.set(transactionAttributeSource.getTransactionAttribute(
                    ((MethodSignature) joinPoint.getSignature()).getMethod(), joinPoint.getTarget().getClass()));
        }

        try {
            return joinPoint.proceed();
        } finally {
            if (outermost) {
                CURRENT.remove();
            }
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - ORDER_OFFSET;
    }
}

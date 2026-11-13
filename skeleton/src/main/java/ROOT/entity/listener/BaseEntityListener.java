package {{rootPackage}}.entity.listener;

import {{rootPackage}}.entity.common.AuditEntity;
import {{rootPackage}}.service.audit.AuditContext;
import {{rootPackage}}.service.bean.BeanUtility;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

/**
 * Fills the audit columns. JPA instantiates listeners itself, hence the static bean lookup.
 */
public final class BaseEntityListener {

    /**
     * @param entity entity being inserted
     */
    @PrePersist
    public void onPrePersist(final AuditEntity entity) {
        AuditContext audit = BeanUtility.getBean(AuditContext.class);
        entity.setCreatedBy(audit.resolveActor());
        entity.setCreatedUtc(audit.now());
    }

    /**
     * @param entity entity being updated
     */
    @PreUpdate
    public void onPreUpdate(final AuditEntity entity) {
        AuditContext audit = BeanUtility.getBean(AuditContext.class);
        entity.setUpdatedBy(audit.resolveActor());
        entity.setUpdatedUtc(audit.now());
    }
}

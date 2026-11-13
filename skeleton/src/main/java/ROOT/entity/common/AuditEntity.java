package {{rootPackage}}.entity.common;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;

/**
 * Audit columns, filled by {@code BaseEntityListener}.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@MappedSuperclass
public abstract class AuditEntity extends InternalEntity {

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "created_utc", nullable = false)
    private Date createdUtc;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_utc")
    private Date updatedUtc;
}

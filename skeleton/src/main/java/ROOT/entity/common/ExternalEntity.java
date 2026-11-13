package {{rootPackage}}.entity.common;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Adds the public {@code entity_id} — the only id the API exposes.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@MappedSuperclass
public abstract class ExternalEntity extends AuditEntity {

    @Column(name = "entity_id", nullable = false, unique = true)
    private String entityId;
}

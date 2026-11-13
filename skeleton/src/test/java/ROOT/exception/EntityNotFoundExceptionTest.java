package {{rootPackage}}.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EntityNotFoundExceptionTest {

    @Test
    void shouldCarryMessage() {
        assertThat(new EntityNotFoundException("missing")).hasMessage("missing");
    }

    @Test
    void shouldRejectEmptyMessage() {
        assertThatThrownBy(() -> new EntityNotFoundException("")).isInstanceOf(IllegalArgumentException.class);
    }
}

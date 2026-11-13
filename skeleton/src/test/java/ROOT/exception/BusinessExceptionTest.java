package {{rootPackage}}.exception;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BusinessExceptionTest {

    @Test
    void shouldCarrySingleMessage() {
        assertThat(new BusinessException("rule").getMessages()).containsExactly("rule");
    }

    @Test
    void shouldCarryAllMessages() {
        BusinessException exception = new BusinessException(List.of("a", "b"));

        assertThat(exception.getMessages()).containsExactly("a", "b");
        assertThat(exception.getMessage()).isEqualTo("a; b");
    }

    @Test
    void shouldRejectMissingMessages() {
        assertThatThrownBy(() -> new BusinessException(" ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new BusinessException(List.of())).isInstanceOf(IllegalArgumentException.class);
    }
}

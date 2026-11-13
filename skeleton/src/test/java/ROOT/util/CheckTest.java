package {{rootPackage}}.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CheckTest {

    @Test
    void shouldAcceptPresentValues() {
        assertThatCode(() -> {
            Check.isNotNull(new Object(), "value");
            Check.isNotEmpty("x", "value");
            Check.isNotEmpty(new char[]{'x'}, "value");
            Check.isNotEmpty(List.of("x"), "value");
        }).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectNull() {
        assertThatThrownBy(() -> Check.isNotNull(null, "value"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Parameter named value is null.");
        assertThatThrownBy(() -> Check.isNotEmpty((String) null, "value"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectEmptyValues() {
        assertThatThrownBy(() -> Check.isNotEmpty("  ", "value"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Parameter named value is empty.");
        assertThatThrownBy(() -> Check.isNotEmpty(new char[0], "value"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Check.isNotEmpty(List.of(), "value"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

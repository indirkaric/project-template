package {{rootPackage}}.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StringHelperTest {

    @Test
    void shouldTreatNullAndBlankAsEmpty() {
        assertThat(StringHelper.isEmpty(null)).isTrue();
        assertThat(StringHelper.isEmpty(" ")).isTrue();
        assertThat(StringHelper.isEmpty("x")).isFalse();
    }
}

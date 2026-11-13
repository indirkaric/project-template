package {{rootPackage}}.security;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class WWWAuthenticateHeaderBuilderTest {

    @Test
    void shouldJoinParametersInOrder() {
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("error", "insufficient_scope");
        parameters.put("error_description", "more");

        assertThat(WWWAuthenticateHeaderBuilder.build(parameters))
                .isEqualTo("Bearer error=\"insufficient_scope\", error_description=\"more\"");
    }

    @Test
    void shouldReturnBareSchemeWithoutParameters() {
        assertThat(WWWAuthenticateHeaderBuilder.build(Map.of())).isEqualTo("Bearer ");
    }
}

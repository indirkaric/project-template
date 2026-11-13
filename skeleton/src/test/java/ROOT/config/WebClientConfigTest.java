package {{rootPackage}}.config;

import {{rootPackage}}.constants.Constants;
import {{rootPackage}}.service.security.IScopeContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebClientConfigTest {

    @Mock
    private IScopeContext scopeContext;

    private WebClientConfig config;

    private final AtomicReference<ClientRequest> sent = new AtomicReference<>();

    private final ExchangeFunction capture = request -> {
        sent.set(request);
        return Mono.just(ClientResponse.create(HttpStatus.OK).build());
    };

    @BeforeEach
    void setUp() {
        config = new WebClientConfig(scopeContext);
    }

    @Test
    void shouldAddCorrelationIdAndUserOnEveryRequest() {
        when(scopeContext.getCorrelationId()).thenReturn("corr-1", "corr-2");
        when(scopeContext.getSqaRequestUserId()).thenReturn("U000001");
        when(scopeContext.getBase64EncodedUserInfo()).thenReturn("encoded");

        send();
        assertThat(sent.get().headers().getFirst(Constants.SQA_REQUEST_ID)).isEqualTo("corr-1");
        assertThat(sent.get().headers().getFirst(Constants.SQA_USER_ID)).isEqualTo("encoded");

        send();
        assertThat(sent.get().headers().getFirst(Constants.SQA_REQUEST_ID)).isEqualTo("corr-2");
    }

    @Test
    void shouldOmitUserHeaderWithoutForwardedUser() {
        when(scopeContext.getCorrelationId()).thenReturn(null);
        when(scopeContext.getSqaRequestUserId()).thenReturn(null);

        send();

        assertThat(sent.get().headers()).doesNotContainKeys(Constants.SQA_REQUEST_ID, Constants.SQA_USER_ID);
    }

    @Test
    void shouldBuildWebClient() {
        assertThat(config.webClient()).isNotNull();
    }

    @Test
    void shouldRejectNullScopeContext() {
        assertThatThrownBy(() -> new WebClientConfig(null)).isInstanceOf(IllegalArgumentException.class);
    }

    private void send() {
        config.propagateSqaHeaders()
                .filter(ClientRequest.create(HttpMethod.GET, URI.create("http://downstream/x")).build(), capture)
                .block();
    }
}

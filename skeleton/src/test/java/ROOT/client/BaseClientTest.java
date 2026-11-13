package {{rootPackage}}.client;

import {{rootPackage}}.constants.Constants;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BaseClientTest {

    private final AtomicReference<ClientRequest> sent = new AtomicReference<>();

    private final WebClient webClient = WebClient.builder().exchangeFunction(request -> {
        sent.set(request);
        return Mono.just(ClientResponse.create(HttpStatus.OK).build());
    }).build();

    @Test
    void shouldBindBaseUriAndClientId() {
        new TestClient(webClient).webClient().get().uri("/v1/items").retrieve().toBodilessEntity().block();

        assertThat(sent.get().url().toString()).isEqualTo("http://downstream.local/api/v1/items");
        assertThat(sent.get().headers().getFirst(Constants.SQA_CLIENT_ID_HEADER)).isEqualTo("Test.Downstream");
    }

    @Test
    void shouldSuppressOnlyListedStatuses() {
        WebClientResponseException notFound = WebClientResponseException.create(
                HttpStatus.NOT_FOUND.value(), "nf", HttpHeaders.EMPTY, new byte[0], null);
        WebClientResponseException serverError = WebClientResponseException.create(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), "err", HttpHeaders.EMPTY, new byte[0], null);

        assertThat(TestClient.notFoundAsEmpty().apply(notFound).blockOptional()).isEmpty();
        assertThatThrownBy(() -> TestClient.notFoundAsEmpty().apply(serverError).block())
                .isSameAs(serverError);
    }

    @Test
    void shouldRequireStatusesAndArguments() {
        assertThatThrownBy(TestClient::nothingSuppressed).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TestClient(null)).isInstanceOf(IllegalArgumentException.class);
    }

    private static final class TestClient extends BaseClient {

        TestClient(final WebClient webClient) {
            super(webClient, "http://downstream.local/api", "Test.Downstream");
        }

        static Function<WebClientResponseException, Mono<Object>> notFoundAsEmpty() {
            return suppressErrors(HttpStatus.NOT_FOUND);
        }

        static Object nothingSuppressed() {
            return suppressErrors();
        }
    }
}

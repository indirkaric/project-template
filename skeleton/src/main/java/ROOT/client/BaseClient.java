package {{rootPackage}}.client;

import {{rootPackage}}.constants.Constants;
import {{rootPackage}}.util.Check;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Base of every {@code client/<downstream>/<Downstream>Client}. Binds the shared WebClient to one
 * downstream base URI and its {@code Sqa-Client-Id}, so no call can forget the header.
 */
public abstract class BaseClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseClient.class);

    private final WebClient webClient;

    /**
     * @param webClient shared WebClient from {@code WebClientConfig}
     * @param baseUri downstream base URI, from {@code client.<name>.base-uri}
     * @param clientId outbound id, format "&lt;ThisService&gt;.&lt;Downstream&gt;"
     */
    protected BaseClient(final WebClient webClient, final String baseUri, final String clientId) {
        Check.isNotNull(webClient, "webClient");
        Check.isNotEmpty(baseUri, "baseUri");
        Check.isNotEmpty(clientId, "clientId");
        this.webClient = webClient.mutate()
                .baseUrl(baseUri)
                .defaultHeader(Constants.SQA_CLIENT_ID_HEADER, clientId)
                .build();
    }

    /**
     * @return WebClient bound to this downstream
     */
    protected final WebClient webClient() {
        return webClient;
    }

    /**
     * Opt-in only: turns the given statuses into an empty result. An empty result then means
     * "not there", never "succeeded", so callers must handle it explicitly.
     *
     * @param statuses statuses to swallow, at least one
     * @param <T> body type
     * @return handler for {@code onErrorResume(WebClientResponseException.class, ...)}
     */
    protected static <T> Function<WebClientResponseException, Mono<T>> suppressErrors(
            final HttpStatus... statuses) {
        Check.isNotNull(statuses, "statuses");
        if (statuses.length == 0) {
            throw new IllegalArgumentException("Parameter named statuses is empty.");
        }

        Set<Integer> suppressed = Arrays.stream(statuses).map(HttpStatus::value).collect(Collectors.toSet());
        return exception -> {
            if (suppressed.contains(exception.getStatusCode().value())) {
                LOGGER.warn("Suppressed downstream error {}: {}", exception.getStatusCode().value(),
                        exception.getResponseBodyAsString());
                return Mono.empty();
            }

            return Mono.error(exception);
        };
    }
}

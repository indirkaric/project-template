package {{rootPackage}}.config;

import {{rootPackage}}.constants.Constants;
import {{rootPackage}}.service.security.IScopeContext;
import {{rootPackage}}.util.Check;
import {{rootPackage}}.util.StringHelper;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

import java.time.Duration;

/**
 * The single outbound {@link WebClient}. {@code Sqa-request-id} and {@code Sqa-user-id} are added here,
 * per request; {@code Sqa-Client-Id} is added once per downstream by {@code BaseClient}.
 */
@Configuration
public class WebClientConfig {

    private static final int MAX_ATTEMPTS = 3;

    private static final int RETRY_DELAY_SECONDS = 5;

    private static final int MAX_IN_MEMORY_BYTES = 10 * 4608 * 4608;

    private static final int RESPONSE_TIMEOUT_MINUTES = 10;

    private static final int CONNECT_TIMEOUT_MILLIS = 30_000;

    private static final int READ_WRITE_TIMEOUT_SECONDS = 600;

    private final IScopeContext scopeContext;

    /**
     * @param scopeContext scope context
     */
    public WebClientConfig(final IScopeContext scopeContext) {
        Check.isNotNull(scopeContext, "scopeContext");
        this.scopeContext = scopeContext;
    }

    /**
     * Designed for extension.
     *
     * @return the shared WebClient
     */
    @Bean
    public WebClient webClient() {
        HttpClient http = HttpClient.create()
                .followRedirect(true)
                .responseTimeout(Duration.ofMinutes(RESPONSE_TIMEOUT_MINUTES))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MILLIS)
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(READ_WRITE_TIMEOUT_SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(READ_WRITE_TIMEOUT_SECONDS)));

        // Header filter first: it is the outer one, so it runs once on the calling thread and the
        // retry filter re-sends the already-enriched request.
        return WebClient.builder()
                .filter(propagateSqaHeaders())
                .filter(retryOnUnavailable())
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(MAX_IN_MEMORY_BYTES))
                        .build())
                .clientConnector(new ReactorClientHttpConnector(http))
                .build();
    }

    /**
     * Reads the scope context when the request is sent, never at bean creation. Callers must subscribe
     * on the request thread ({@code block()}), because the values live in ThreadLocals.
     *
     * @return filter adding {@code Sqa-request-id} and {@code Sqa-user-id}
     */
    final ExchangeFilterFunction propagateSqaHeaders() {
        return (request, next) -> {
            String correlationId = scopeContext.getCorrelationId();
            boolean hasUser = !StringHelper.isEmpty(scopeContext.getSqaRequestUserId());
            String userInfo = hasUser ? scopeContext.getBase64EncodedUserInfo() : null;
            return next.exchange(ClientRequest.from(request).headers(headers -> {
                if (!StringHelper.isEmpty(correlationId)) {
                    headers.set(Constants.SQA_REQUEST_ID, correlationId);
                }

                if (userInfo != null && !headers.containsKey(Constants.SQA_USER_ID)) {
                    headers.set(Constants.SQA_USER_ID, userInfo);
                }
            }).build());
        };
    }

    private ExchangeFilterFunction retryOnUnavailable() {
        return (request, next) -> next.exchange(request)
                .flatMap(response -> response.statusCode().value() == HttpStatus.SERVICE_UNAVAILABLE.value()
                        || response.statusCode().value() == HttpStatus.GATEWAY_TIMEOUT.value()
                        ? response.createException().flatMap(Mono::error)
                        : Mono.just(response))
                .retryWhen(Retry.backoff(MAX_ATTEMPTS, Duration.ofSeconds(RETRY_DELAY_SECONDS)));
    }
}

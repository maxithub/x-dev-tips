package max.lab.xdevfeign;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.RequestInterceptor;
import feign.Response;
import feign.codec.Decoder;
import feign.codec.ErrorDecoder;
import feign.optionals.OptionalDecoder;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.openfeign.encoding.FeignAcceptGzipEncodingInterceptor;
import org.springframework.cloud.openfeign.encoding.FeignClientEncodingProperties;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.zip.GZIPInputStream;

import static java.util.Collections.singletonList;

@Configuration
public class FeignConfiguration {
    private Response wrapResponse(Response response) throws IOException {
        var theResponse = response;
        var headers = response.headers();
        var body = response.body();
        var bodyInputStream = body != null ? body.asInputStream() : null;
        if (headers != null
                && headers.getOrDefault("Content-Encoding", headers.getOrDefault("content-encoding", singletonList("")))
                    .iterator().next().toLowerCase().equals("gzip")
                && bodyInputStream != null
                && !(bodyInputStream instanceof GZIPInputStream)) {
            theResponse = response.toBuilder().body(new GZIPInputStream(bodyInputStream), body.length()).build();
        }
        return theResponse;
    }

    @Bean
    public Decoder decoder(ObjectFactory<HttpMessageConverters> messageConverters) {
        return new OptionalDecoder(
                new ResponseEntityDecoder(new SpringDecoder(messageConverters) {
                    @Override
                    public Object decode(Response response, Type type) throws IOException, FeignException {
                        return super.decode(wrapResponse(response), type);
                    }
                })
        );
    }

    @Bean
    public ErrorDecoder errorDecoder(ObjectMapper objectMapper) {
        return new ErrorDecoder.Default() {
            @Override
            public Exception decode(String methodKey, Response response) {
                var theResponse = response;
                try {
                    theResponse = wrapResponse(response);
                } catch (Exception e) { }

                return super.decode(methodKey, theResponse);
            }
        };
    }

    @ConditionalOnProperty(value = "feign.compression.response.enabled", havingValue = "true")
    @Bean
    public RequestInterceptor feignAcceptGzipEncodingInterceptor() {
        return new FeignAcceptGzipEncodingInterceptor(new FeignClientEncodingProperties()) { };
    }

    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> defaultCustomizer() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .circuitBreakerConfig(
                        CircuitBreakerConfig.from(CircuitBreakerConfig.ofDefaults()).ignoreExceptions(
                                FeignException.FeignClientException.class
                        ).build()
                ).build());
    }

}

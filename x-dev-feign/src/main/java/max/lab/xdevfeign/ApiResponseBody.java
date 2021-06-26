package max.lab.xdevfeign;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException.FeignClientException;
import org.slf4j.Logger;

import java.nio.ByteBuffer;

public interface ApiResponseBody {
    default Error getError() {
        return null;
    }

    default void setError(Error error) { }


    static <T extends ApiResponseBody> T fallback(ObjectMapper objectMapper,
                                                  Throwable cause,
                                                  Class<T> clazz,
                                                  T defaultValue,
                                                  Logger log,
                                                  String template,
                                                  Object... args) {
        var apiResponseBody = defaultValue;
        if (cause instanceof FeignClientException) {
            FeignClientException exception = (FeignClientException) cause;
            var bytes = exception.responseBody().map(ByteBuffer::array).orElse(null);
            if (bytes != null) {
                try {
                    Error error = objectMapper.readValue(bytes, Error.class);
                    apiResponseBody = clazz.getDeclaredConstructor().newInstance();
                    apiResponseBody.setError(error);
                } catch (Exception e) {
                    log.error("Failed to create object for type {} or parse error from response body", clazz, e);
                }
            }
        } else {
            log.error(template, args);
        }
        return apiResponseBody;
    }
}

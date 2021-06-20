package max.lab.xdevfeign;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
public class Error {
    private final List<InvalidField> invalidFields;
    private final List<String> errors;

    @RequiredArgsConstructor
    @Data
    public static class InvalidField {
        private final String name;
        private final String message;
    }
}

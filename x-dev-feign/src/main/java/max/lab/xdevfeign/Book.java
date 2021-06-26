package max.lab.xdevfeign;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class Book implements ApiResponseBody {
    private String isbn;
    private String title;
    private LocalDate dateOfPublish;
    private BigDecimal price;

    private Error error;
}

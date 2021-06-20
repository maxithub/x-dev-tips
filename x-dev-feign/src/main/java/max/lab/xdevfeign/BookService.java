package max.lab.xdevfeign;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

//@FeignClient(name = "bookService", url = "http://localhost:8081", fallbackFactory = BookService.FallbackImpl.class)
@FeignClient(name = "bookService", url = "http://localhost:8081")
public interface BookService {
    Book FALLBACK_BOOK = new Book();

    @PostMapping("/book")
    Book createBook(@RequestBody Book book);

    @GetMapping("/book/{isbn}")
    Book findBook(@PathVariable String isbn);

    @Slf4j
    @Component
    class FallbackImpl implements FallbackFactory<BookService> {

        @Override
        public BookService create(Throwable cause) {
            return new BookService() {
                @Override
                public Book createBook(Book book) {
                    log.error("Failed to create book", cause);
                    return FALLBACK_BOOK;
                }

                @Override
                public Book findBook(String isbn) {
                    log.error("Failed to find book", cause);
                    return FALLBACK_BOOK;
                }
            };
        }
    }
}

package max.lab.xdevfeign;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@Slf4j
public class MainController {
    private final BookService service;

    @PostMapping("/book")
    public Book createBook(@RequestBody Book book) {
//        return service.createBook(book);
        var theBook = BookService.FALLBACK_BOOK;
        try {
            theBook = service.createBook(book);
            if (theBook.getError() != null) {
                log.error("Failed to create book, got error message: {}", theBook.getError());
            }
        } catch (Exception e) {
            log.error("Other exception", e);
        }
        return theBook;
    }

    @GetMapping("/book/{isbn}")
    public Book findBook(@PathVariable String isbn) {
        return service.findBook(isbn);
    }
}

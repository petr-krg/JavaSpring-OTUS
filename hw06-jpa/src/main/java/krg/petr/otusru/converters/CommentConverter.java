package krg.petr.otusru.converters;

import krg.petr.otusru.models.Comment;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class CommentConverter {

    public String commentToString(Comment comment) {
        var book = comment.getBook();

        String bookInfo = Objects.nonNull(book)
                ? "Id: %d, title: %s".formatted(book.getId(), book.getTitle())
                : "null";

        return "Id: %d, text: %s, book: [%s]".formatted(
                comment.getId(),
                comment.getText(),
                bookInfo
        );
    }
}
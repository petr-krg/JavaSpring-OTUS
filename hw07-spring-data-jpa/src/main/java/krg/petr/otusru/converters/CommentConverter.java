package krg.petr.otusru.converters;

import krg.petr.otusru.models.Comment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CommentConverter {

    public String commentToString(Comment comment) {
        return "Id: %d, Book title: %s, Comment text: %s".formatted(comment.getId(),
                comment.getBook().getTitle(),
                comment.getText());
    }
}
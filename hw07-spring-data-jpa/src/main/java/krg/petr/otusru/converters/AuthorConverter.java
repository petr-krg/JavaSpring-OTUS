package krg.petr.otusru.converters;

import krg.petr.otusru.models.Author;
import org.springframework.stereotype.Component;

@Component
public class AuthorConverter {
    public String authorToString(Author author) {
        return "Id: %d, FullName: %s".formatted(author.getId(), author.getFullName());
    }
}
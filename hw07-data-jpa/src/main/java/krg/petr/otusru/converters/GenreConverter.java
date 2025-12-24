package krg.petr.otusru.converters;

import org.springframework.stereotype.Component;
import krg.petr.otusru.models.Genre;

@Component
public class GenreConverter {
    public String genreToString(Genre genre) {
        return "Id: %d, Name: %s".formatted(genre.getId(), genre.getName());
    }
}
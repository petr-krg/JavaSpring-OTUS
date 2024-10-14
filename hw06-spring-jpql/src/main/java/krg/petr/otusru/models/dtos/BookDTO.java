package krg.petr.otusru.models.dtos;

import krg.petr.otusru.models.Book;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class BookDTO {

    private Long id;

    private String title;

    private AuthorDTO author;

    private List<GenreDTO> genreName;

    public BookDTO(Book book) {
        this.id = book.getId();
        this.title = book.getTitle();
        this.author = new AuthorDTO(book.getAuthor());
        this.genreName = book.getGenres().stream().map(GenreDTO::new).toList();
    }
}
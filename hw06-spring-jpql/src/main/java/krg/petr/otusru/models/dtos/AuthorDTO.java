package krg.petr.otusru.models.dtos;

import krg.petr.otusru.models.Author;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthorDTO {

    private Long id;

    private String fullName;

    public AuthorDTO(Author author) {
        this.id = author.getId();
        this.fullName = author.getFullName();
    }
}
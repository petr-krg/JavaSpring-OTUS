package krg.petr.otusru.models.dtos;


import krg.petr.otusru.models.Genre;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GenreDTO {

    private Long id;

    private String name;

    public GenreDTO(Genre genre) {
        this.id = genre.getId();
        this.name = genre.getName();
    }
}
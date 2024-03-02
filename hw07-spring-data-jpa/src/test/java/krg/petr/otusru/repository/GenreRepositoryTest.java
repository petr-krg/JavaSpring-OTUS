package krg.petr.otusru.repository;

import jakarta.persistence.TypedQuery;
import krg.petr.otusru.models.Genre;
import krg.petr.otusru.repositories.GenreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий на основе Jdbc для работы с жанрами книг ")
@DataJpaTest
public class GenreRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private GenreRepository genreRepository;


    @DisplayName("должен загружать список всех жанров")
    @Test
    void shouldReturnCorrectGenreList() {
        TypedQuery<Genre> query = entityManager
                .getEntityManager()
                .createQuery("SELECT g FROM Genre g", Genre.class);

        var actualGenres = genreRepository.findAll();
        var expectedGenres = query.getResultList();

        assertThat(actualGenres).containsExactlyElementsOf(expectedGenres);
        actualGenres.forEach(System.out::println);
    }

    @DisplayName("должен загружать жанр по списку id")
    @Test
    void shouldReturnCorrectGenreById() {
        Genre expectedGenre = entityManager.find(Genre.class, 1L);
        var actualGenres = genreRepository.findAllByIds(Set.of(expectedGenre.getId()));
        //noinspection OptionalGetWithoutIsPresent
        assertThat(actualGenres.
                stream()
                .findAny()
                .get())
                .isEqualTo(expectedGenre);
    }
}
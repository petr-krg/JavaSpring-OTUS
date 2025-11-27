package krg.petr.otusru.repositories;

import krg.petr.otusru.models.Genre;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий на основе Jpa для работы с жанрами")
@DataJpaTest
@Import(JpaGenreRepository.class)
public class JpaGenreRepositoryTest {

    @Autowired
    private GenreRepository genreRepository;

    @DisplayName("должен загружать список всех жанров")
    @Test
    void shouldReturnAllGenres() {
        var expectedGenres = getDbGenres();
        var actualGenres = genreRepository.findAll();

        assertThat(actualGenres)
                .hasSize(expectedGenres.size())
                .containsExactlyElementsOf(expectedGenres);
    }

    @DisplayName("должен находить жанры по набору идентификаторов")
    @Test
    void shouldReturnGenresByIds() {
        var expectedGenres = getDbGenres();
        var ids = Set.of(1L, 3L, 5L);

        var actualGenres = genreRepository.findAllByIds(ids);

        assertThat(actualGenres)
                .hasSize(ids.size())
                .containsExactlyInAnyOrder(
                        expectedGenres.get(0),
                        expectedGenres.get(2),
                        expectedGenres.get(4)
                );
    }

    @DisplayName("должен возвращать пустой список, если набор идентификаторов пуст")
    @Test
    void shouldReturnEmptyListIfIdsEmpty() {
        var actualGenres = genreRepository.findAllByIds(Set.of());
        assertThat(actualGenres).isEmpty();
    }

    private static List<Genre> getDbGenres() {
        return IntStream.range(1, 7).boxed()
                .map(id -> new Genre(id, "Genre_" + id))
                .toList();
    }
}
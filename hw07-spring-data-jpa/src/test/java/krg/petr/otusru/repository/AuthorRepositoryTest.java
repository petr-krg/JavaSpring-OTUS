package krg.petr.otusru.repository;

import jakarta.persistence.TypedQuery;
import krg.petr.otusru.models.Author;
import krg.petr.otusru.repositories.AuthorRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий на основе Jdbc для работы с авторами книг ")
@DataJpaTest
public class AuthorRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AuthorRepository authorRepository;

    @DisplayName("должен загружать список всех авторов")
    @Test
    void shouldReturnCorrectAuthorList() {
        TypedQuery<Author> query = entityManager
                .getEntityManager()
                .createQuery("SELECT a FROM Author a", Author.class);

        var expectedAuthors = query.getResultList();
        var actualAuthors = authorRepository.findAll();

        assertThat(actualAuthors).containsExactlyElementsOf(expectedAuthors);
        actualAuthors.forEach(System.out::println);
    }

    @DisplayName("должен загружать автора по id")
    @Test
    void shouldReturnCorrectAuthorById() {
        Author expectedAuthor = entityManager.find(Author.class, 1L);
        var actualAuthors = authorRepository.findById(expectedAuthor.getId());
        assertThat(actualAuthors)
                .isPresent()
                .get()
                .isEqualTo(expectedAuthor);
    }
}
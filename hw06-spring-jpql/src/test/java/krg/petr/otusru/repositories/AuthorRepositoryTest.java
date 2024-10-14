package krg.petr.otusru.repositories;

import jakarta.persistence.TypedQuery;
import krg.petr.otusru.models.Author;
import krg.petr.otusru.repositories.impl.AuthorRepositoryImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий на основе Jdbc для работы с авторами книг ")
@DataJpaTest
@Import(AuthorRepositoryImpl.class)
public class AuthorRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AuthorRepositoryImpl authorRepository;

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
        var actualAuthorsOptional = authorRepository.findById(expectedAuthor.getId());
        assertThat(actualAuthorsOptional).isPresent();
        var actualAuthor = actualAuthorsOptional.get();
        assertThat(actualAuthor).isEqualTo(expectedAuthor);
    }
}
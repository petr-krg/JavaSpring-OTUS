package krg.petr.otusru.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import krg.petr.otusru.models.Author;
import krg.petr.otusru.models.Book;
import krg.petr.otusru.models.Genre;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий на основе Jpa для работы с книгами ")
@DataJpaTest
@Import(JpaBookRepository.class)
class JpaBookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private TestEntityManager em;

    private List<Author> dbAuthors;
    private List<Genre> dbGenres;
    private List<Book> dbBooks;

    @BeforeEach
    void setUp() {
        dbAuthors = getDbAuthors();
        dbGenres = getDbGenres();
        dbBooks = buildDbBooks(dbAuthors, dbGenres);
    }

    @DisplayName("должен загружать книгу по id")
    @ParameterizedTest
    @MethodSource("getExpectedBooks")
    void shouldReturnCorrectBookById(Book expectedBook) {
        var actualBook = bookRepository.findById(expectedBook.getId());

        assertThat(actualBook).isPresent()
                .get()
                .isEqualTo(expectedBook);
    }

    @DisplayName("должен загружать список всех книг")
    @Test
    void shouldReturnCorrectBooksList() {
        var actualBooks = bookRepository.findAll();
        var expectedBooks = dbBooks;

        assertThat(actualBooks).containsExactlyElementsOf(expectedBooks);
        actualBooks.forEach(System.out::println);
    }

    @DisplayName("должен сохранять новую книгу")
    @Test
    void shouldSaveNewBook() {
        // Берём управляемые сущности из БД
        Author author = em.find(Author.class, 1L);
        Genre genre1 = em.find(Genre.class, 1L);
        Genre genre2 = em.find(Genre.class, 3L);

        var expectedBook = new Book(
                0L,
                "BookTitle_10500",
                author,
                List.of(genre1, genre2)
        );

        var returnedBook = bookRepository.save(expectedBook);

        assertThat(returnedBook).isNotNull();
        assertThat(returnedBook.getId()).isGreaterThan(0);

        var bookFromDbOpt = bookRepository.findById(returnedBook.getId());
        assertThat(bookFromDbOpt).isPresent();
        var bookFromDb = bookFromDbOpt.get();

        assertThat(bookFromDb.getTitle()).isEqualTo("BookTitle_10500");
        assertThat(bookFromDb.getAuthor().getId()).isEqualTo(author.getId());
        assertThat(bookFromDb.getGenres())
                .extracting(Genre::getId)
                .containsExactlyInAnyOrder(genre1.getId(), genre2.getId());
    }

    @DisplayName("должен сохранять измененную книгу")
    @Test
    void shouldSaveUpdatedBook() {
        var existingBook = bookRepository.findById(1L).orElseThrow();

        Author newAuthor = em.find(Author.class, 3L);
        Genre genre4 = em.find(Genre.class, 5L);
        Genre genre5 = em.find(Genre.class, 6L);

        var updatedBook = new Book(
                existingBook.getId(),
                "BookTitle_10500",
                newAuthor,
                List.of(genre4, genre5)
        );

        var returnedBook = bookRepository.save(updatedBook);

        assertThat(returnedBook).isNotNull();
        assertThat(returnedBook.getId()).isEqualTo(existingBook.getId());

        var bookFromDbOpt = bookRepository.findById(existingBook.getId());
        assertThat(bookFromDbOpt).isPresent();
        var bookFromDb = bookFromDbOpt.get();

        assertThat(bookFromDb.getTitle()).isEqualTo("BookTitle_10500");
        assertThat(bookFromDb.getAuthor().getId()).isEqualTo(newAuthor.getId());
        assertThat(bookFromDb.getGenres())
                .extracting(Genre::getId)
                .containsExactlyInAnyOrder(genre4.getId(), genre5.getId());
    }

    @DisplayName("должен удалять книгу по id ")
    @Test
    void shouldDeleteBook() {
        assertThat(bookRepository.findById(1L)).isPresent();
        bookRepository.deleteById(1L);
        assertThat(bookRepository.findById(1L)).isEmpty();
    }

    private static List<Book> getExpectedBooks() {
        var dbAuthors = getDbAuthors();
        var dbGenres = getDbGenres();
        return buildDbBooks(dbAuthors, dbGenres);
    }

    private static List<Author> getDbAuthors() {
        return IntStream.range(1, 4).boxed()
                .map(id -> new Author(id, "Author_" + id))
                .toList();
    }

    private static List<Genre> getDbGenres() {
        return IntStream.range(1, 7).boxed()
                .map(id -> new Genre(id, "Genre_" + id))
                .toList();
    }

    private static List<Book> buildDbBooks(List<Author> dbAuthors, List<Genre> dbGenres) {
        return IntStream.range(1, 4).boxed()
                .map(id -> new Book(
                        id,
                        "BookTitle_" + id,
                        dbAuthors.get(id - 1),
                        List.copyOf(dbGenres.subList((id - 1) * 2, (id - 1) * 2 + 2))
                ))
                .toList();
    }
}
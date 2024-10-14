package krg.petr.otusru.repositories;

import krg.petr.otusru.repositories.impl.BookRepositoryImpl;
import krg.petr.otusru.repositories.impl.GenreRepositoryImpl;
import krg.petr.otusru.utils.TestDataGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import krg.petr.otusru.models.Author;
import krg.petr.otusru.models.Book;
import krg.petr.otusru.models.Genre;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий на основе JPA для работы с книгами ")
@DataJpaTest
@Import({BookRepositoryImpl.class, GenreRepositoryImpl.class})
class BookRepositoryNeedTest {

    @Autowired
    private BookRepositoryImpl bookRepository;

    private List<Author> dbAuthors;

    private List<Genre> dbGenres;

    private List<Book> dbBooks;

    @BeforeEach
    void setUp() {
        dbAuthors = TestDataGenerator.getDbAuthors();
        dbGenres = TestDataGenerator.getDbGenres();
        dbBooks = TestDataGenerator.getDbBooks(dbAuthors, dbGenres);
    }

    @DisplayName("должен загружать книгу по id")
    @ParameterizedTest
    @MethodSource("krg.petr.otusru.utils.TestDataGenerator#getDbBooks")
    void shouldReturnCorrectBookById(Book expectedBook) {
        var actualBook = bookRepository.findById(expectedBook.getId());
        assertThat(actualBook)
                .isPresent()
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
        var expectedBook = new Book(null, "BookTitle_10500", dbAuthors.get(0),
                List.of(dbGenres.get(0), dbGenres.get(2)));
        var returnedBook = bookRepository.save(expectedBook);
        assertThat(returnedBook).isNotNull()
                .matches(book -> book.getId() > 0)
                .usingRecursiveComparison().ignoringExpectedNullFields().isEqualTo(expectedBook);

        assertThat(bookRepository.findById(returnedBook.getId()))
                .isPresent()
                .get()
                .matches(book -> book.getTitle().equals(returnedBook.getTitle()));
                //.isEqualTo(returnedBook);
    }

    @DisplayName("должен сохранять измененную книгу")
    @Test
    void shouldSaveUpdatedBook() {
        var expectedBook = new Book(1L, "BookTitle_10500", dbAuthors.get(2),
                List.of(dbGenres.get(4), dbGenres.get(5)));


        assertThat(bookRepository.findById(expectedBook.getId()))
                .isPresent()
                .get()
                .matches(book -> !book.getTitle().equals(expectedBook.getTitle()));
                //.isNotEqualTo(expectedBook);

        var returnedBook = bookRepository.save(expectedBook);
        assertThat(returnedBook).isNotNull()
                .matches(book -> book.getId() > 0)
                .usingRecursiveComparison().ignoringExpectedNullFields()
                .isEqualTo(expectedBook);

        assertThat(bookRepository.findById(returnedBook.getId()))
                .isPresent()
                .get()
                .matches(book -> book.getTitle().equals(returnedBook.getTitle()));
    }

    @DisplayName("должен удалять книгу по id ")
    @Test
    void shouldDeleteBook() {
        assertThat(bookRepository.findById(1L)).isPresent();
        bookRepository.deleteById(1L);
        assertThat(bookRepository.findById(1L)).isEmpty();
    }
}
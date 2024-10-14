package krg.petr.otusru.services;

import krg.petr.otusru.exceptions.EntityNotFoundException;
import krg.petr.otusru.models.Author;
import krg.petr.otusru.models.Book;
import krg.petr.otusru.models.Genre;
import krg.petr.otusru.repositories.impl.AuthorRepositoryImpl;
import krg.petr.otusru.repositories.impl.BookRepositoryImpl;
import krg.petr.otusru.repositories.impl.GenreRepositoryImpl;
import krg.petr.otusru.services.impl.AuthorServiceImpl;
import krg.petr.otusru.services.impl.BookServiceImpl;
import krg.petr.otusru.services.impl.GenreServiceImpl;
import krg.petr.otusru.utils.TestDataGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DisplayName("Тесты сервиса для работы с книгами")
@DataJpaTest
@Import({BookServiceImpl.class, BookRepositoryImpl.class, AuthorRepositoryImpl.class, AuthorServiceImpl.class,
        GenreRepositoryImpl.class, GenreServiceImpl.class})
@Transactional(propagation = Propagation.NEVER)
public class BookServiceImplTest {

    @Autowired
    private BookServiceImpl bookService;

    @Autowired
    private AuthorRepositoryImpl authorRepository;

    @Autowired
    private GenreRepositoryImpl genreRepository;

    List<Book> dbBooks;

    List<Author> dbAuthors;

    List<Genre> dbGenres;

    @Autowired
    private AuthorService authorService;

    @Autowired
    private GenreService genreService;

    @BeforeEach
    void setUp() {
        dbBooks = TestDataGenerator.getDbBooks();
        dbAuthors = TestDataGenerator.getDbAuthors();
        dbGenres = TestDataGenerator.getDbGenres();
    }

    @DisplayName("должен загружать книгу по id")
    @ParameterizedTest
    @MethodSource("krg.petr.otusru.utils.TestDataGenerator#getDbBooks")
    void shouldReturnCorrectBookById(Book expectedBook) {
        var actualBook = bookService.findById(expectedBook.getId());

        assertThat(actualBook)
                .isPresent()
                .hasValueSatisfying(book -> {
                    assertThat(book.getTitle()).isEqualTo(expectedBook.getTitle());
                    assertThat(book.getAuthor()).isEqualTo(expectedBook.getAuthor());
                    assertThat(book.getGenres()).isNotNull().isNotEmpty()
                            .containsExactlyInAnyOrderElementsOf(expectedBook.getGenres());
                });
    }

    @DisplayName("должен загружать список всех книг")
    @Test
    void shouldReturnCorrectBooksList() {
        var actualBooks = bookService.findAll();
        var expectedBooks = dbBooks;

        assertThat(actualBooks).containsExactlyElementsOf(expectedBooks);
        actualBooks.forEach(book -> {
            assertThat(book.getAuthor()).isNotNull();
            assertThat(book.getGenres()).isNotNull().isNotEmpty();
        });
    }

    @DisplayName("должен сохранять новую книгу")
    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldSaveNewBook() {
        var newBookTitle  = "newBookTitle_100500";
        var authorId = dbAuthors.get(0).getId();
        var genreIds = Set.of(dbGenres.get(2).getId(), dbGenres.get(3).getId());

        var savedBook = bookService.insert(newBookTitle, authorId, genreIds);

        var expectedBook = bookService.findById(savedBook.getId());

        assertThat(savedBook).isNotNull();

        assertThat(savedBook).isNotNull();
        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getTitle()).isEqualTo(newBookTitle);

        assertThat(savedBook.getAuthor()).isNotNull();
        assertThat(savedBook.getAuthor().getId()).isEqualTo(authorId);
        assertThat(savedBook.getAuthor().getFullName()).isEqualTo(expectedBook.get().getAuthor().getFullName());

        assertThat(savedBook.getGenres())
                .isNotNull()
                .hasSize(2)
                .extracting(Genre::getId)
                .containsExactlyInAnyOrderElementsOf(genreIds);

        var expectedGenreNames = genreIds.stream()
                .map(id -> dbGenres.stream()
                        .filter(genre -> genre.getId().equals(id))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Genre not found with ID: " + id))
                        .getName())
                .collect(Collectors.toSet());

        assertThat(savedBook.getGenres())
                .extracting(Genre::getName)
                .containsExactlyInAnyOrderElementsOf(expectedGenreNames);
    }

    @DisplayName("должен сохранять измененную книгу")
    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldSaveUpdatedBook() {
        var updateBookTitle = "updateBookTitle_100500";

        var updateAuthor = authorRepository.findById(dbAuthors.get(1).getId())
                .orElseThrow(() -> new EntityNotFoundException("Author with id %d not found"
                        .formatted(dbAuthors.get(1).getId())));

        var updateGenres = genreRepository.findAllByIds(
                Set.of(dbGenres.get(2).getId(), dbGenres.get(3).getId()));

        var initBook = bookService.findById(1L);
        var bookId = initBook.get().getId();

        bookService.update(bookId, updateBookTitle, initBook.get().getAuthor().getId(),
                Set.of(updateGenres.get(0).getId(), updateGenres.get(1).getId()));

        var actualBook = bookService.findById(bookId);

        assertThat(actualBook).isPresent();
        var updateBook = actualBook.get();

        assertThat(updateBook.getAuthor()).isNotNull();
        assertThat(updateBook.getGenres()).isNotNull();
        assertThat(updateBook.getGenres()).hasSize(2);
        assertThat(updateBook.getGenres())
                .extracting(Genre::getName)
                .containsExactlyInAnyOrder(updateGenres.get(0).getName(), updateGenres.get(1).getName());

        assertDoesNotThrow(() -> updateBook.getAuthor().getFullName());
        updateBook.getGenres().forEach(genre -> assertDoesNotThrow(genre::getName));
    }

    @DisplayName("должен удалять книгу по id ")
    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldDeleteBook() {
        assertThat(bookService.findById(3L)).isPresent();
        bookService.deleteById(3L);
        assertThat(bookService.findById(3L)).isEmpty();
    }
}
package krg.petr.otusru.services;

import krg.petr.otusru.converters.BookConverter;
import krg.petr.otusru.exceptions.EntityNotFoundException;
import krg.petr.otusru.models.Author;
import krg.petr.otusru.models.Book;
import krg.petr.otusru.models.Genre;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

@DataJpaTest
@Import({BookServiceImpl.class})
@DisplayName("Тест сервиса работы с книгами")
public class BookServiceImplTest {

    @Autowired
    private BookService bookService;

    @Autowired
    private TestEntityManager em;

    @MockitoBean
    private BookConverter bookConverter;

    private List<Author> dbAuthors;
    private List<Genre> dbGenres;
    private List<Book> dbBooks;

    @BeforeEach
    void setUp() {
        dbAuthors = getDbAuthors();
        dbGenres = getDbGenres();
        dbBooks = buildDbBooks(dbAuthors, dbGenres);

        when(bookConverter.bookToString(any(Book.class)))
                .thenAnswer(invocation -> {
                    Book book = invocation.getArgument(0);
                    return "Book: id=%d, title=%s".formatted(book.getId(), book.getTitle());
                });
    }

    @Test
    @DisplayName("Загрузка книги по id")
    void shouldReturnCorrectBookById() {
        String result = bookService.findById(1L);

        assertThat(result).contains("Book: id=1");
        assertThat(result).contains("BookTitle_1");
    }

    @Test
    @DisplayName("Возвращаем значение при отсутсвие книги")
    void shouldReturnMessageWhenBookNotFound() {
        String result = bookService.findById(999L);

        assertThat(result).isEqualTo("Book with id 999 not found");
    }

    @Test
    @DisplayName("Загружаем списко всех книг")
    void shouldReturnCorrectBooksList() {
        String result = bookService.findAll();

        assertThat(result).contains("Book: id=1");
        assertThat(result).contains("Book: id=2");
        assertThat(result).contains("Book: id=3");
    }

    @Test
    @DisplayName("Сохраняем новую книгу")
    void shouldSaveNewBook() {
        String result = bookService.merge(0L, "New Book Title", 1L, Set.of(1L, 2L));

        assertThat(result).contains("New Book Title");

        var books = em.getEntityManager()
                .createQuery("SELECT b FROM Book b WHERE b.title = :title", Book.class)
                .setParameter("title", "New Book Title")
                .getResultList();

        assertThat(books).hasSize(1);
        var savedBook = books.get(0);
        assertThat(savedBook.getTitle()).isEqualTo("New Book Title");
        assertThat(savedBook.getAuthor().getId()).isEqualTo(1L);
        assertThat(savedBook.getGenres())
                .extracting(Genre::getId)
                .containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    @DisplayName("Сохраняем измененную книгу")
    void shouldSaveUpdatedBook() {
        String result = bookService.merge(1L, "Updated Book Title", 2L, Set.of(3L, 4L));

        assertThat(result).contains("Updated Book Title");

        var bookFromDb = em.find(Book.class, 1L);

        assertThat(bookFromDb).isNotNull();
        assertThat(bookFromDb.getTitle()).isEqualTo("Updated Book Title");
        assertThat(bookFromDb.getAuthor().getId()).isEqualTo(2L);
        assertThat(bookFromDb.getGenres())
                .extracting(Genre::getId)
                .containsExactlyInAnyOrder(3L, 4L);
    }

    @DisplayName("Выбрасываем исключение при отсутсвие автора")
    @Test
    void shouldThrowExceptionWhenAuthorNotFound() {
        assertThatThrownBy(() -> bookService.merge(0L, "New Book", 999L, Set.of(1L)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Author with id 999 not found");
    }

    @DisplayName("Выбрасываем исключение при отсутсвующих жанрах")
    @Test
    void shouldThrowExceptionWhenGenresNotFound() {
        assertThatThrownBy(() -> bookService.merge(0L, "New Book", 1L, Set.of(999L)))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("One or all genres with ids");
    }

    @DisplayName("Выбрасываем исключение при пустом списке жанров")
    @Test
    void shouldThrowExceptionWhenGenresEmpty() {
        assertThatThrownBy(() -> bookService.merge(0L, "New Book", 1L, Set.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Genres ids must not be null");
    }

    @DisplayName("Удаляем книгу по id")
    @Test
    void shouldDeleteBook() {
        assertThat(em.find(Book.class, 1L)).isNotNull();

        bookService.deleteById(1L);

        em.flush();
        em.clear();
        assertThat(em.find(Book.class, 1L)).isNull();
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
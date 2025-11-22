package krg.petr.otusru.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import krg.petr.otusru.exceptions.EntityNotFoundException;
import krg.petr.otusru.models.Author;
import krg.petr.otusru.models.Book;
import krg.petr.otusru.models.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JdbcBookRepository implements BookRepository {

    private final GenreRepository genreRepository;

    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public Optional<Book> findById(long id) {
        var sql = """
                select b.id as book_id,
                       b.title,
                       a.id        as author_id,
                       a.full_name as author_full_name,
                       g.id        as genre_id,
                       g.name      as genre_name
                from books b
                     join authors a on a.id = b.author_id
                     left join books_genres bg on bg.book_id = b.id
                     left join genres g on g.id = bg.genre_id
                where b.id = :id
                order by g.id
                """;
        var params = Map.of("id", id);
        Book book = jdbc.query(sql, params, new BookResultSetExtractor());
        return Optional.ofNullable(book);
    }

    @Override
    public List<Book> findAll() {
        var genres = genreRepository.findAll();
        var books = getAllBooksWithoutGenres();
        var relations = getAllGenreRelations();
        mergeBooksInfo(books, genres, relations);
        return books;
    }

    @Override
    public Book save(Book book) {
        if (book.getId() == 0) {
            return insert(book);
        }
        return update(book);
    }

    @Override
    public void deleteById(long id) {
        var sql = "delete from books where id = :id";
        jdbc.update(sql, Map.of("id", id));
    }

    private List<Book> getAllBooksWithoutGenres() {
        var sql = """
                select b.id,
                       b.title,
                       a.id        as author_id,
                       a.full_name as author_full_name
                from books b
                     join authors a on a.id = b.author_id
                order by b.id
                """;
        return jdbc.query(sql, new BookRowMapper());
    }

    private List<BookGenreRelation> getAllGenreRelations() {
        var sql = """
                select book_id, genre_id
                from books_genres
                order by book_id, genre_id
                """;
        return jdbc.query(sql, (rs, rowNum) ->
                new BookGenreRelation(
                        rs.getLong("book_id"),
                        rs.getLong("genre_id")
                ));
    }

    private void mergeBooksInfo(List<Book> booksWithoutGenres, List<Genre> genres,
                                List<BookGenreRelation> relations) {
        if (booksWithoutGenres.isEmpty() || genres.isEmpty() || relations.isEmpty()) {
            return;
        }

        Map<Long, Book> bookById = booksWithoutGenres.stream()
                .collect(Collectors.toMap(Book::getId, b -> b));

        Map<Long, Genre> genreById = genres.stream()
                .collect(Collectors.toMap(Genre::getId, g -> g));

        for (BookGenreRelation rel : relations) {
            Book book = bookById.get(rel.bookId());
            Genre genre = genreById.get(rel.genreId());
            if (Objects.nonNull(book) && Objects.nonNull(genre)) {
                if (Objects.isNull(book.getGenres())) {
                    book.setGenres(new ArrayList<>());
                }
                book.getGenres().add(genre);
            }
        }

    }

    private Book insert(Book book) {
        var sql = "insert into books(title, author_id) values (:title, :authorId)";
        var params = new MapSqlParameterSource()
                .addValue("title", book.getTitle())
                .addValue("authorId", book.getAuthor().getId());

        var keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, params, keyHolder, new String[]{"id"});

        book.setId(keyHolder.getKeyAs(Long.class));
        batchInsertGenresRelationsFor(book);
        return book;
    }

    private Book update(Book book) {
        var sql = """
                update books
                set title = :title,
                    author_id = :authorId
                where id = :id
                """;

        var params = Map.of(
                "id", book.getId(),
                "title", book.getTitle(),
                "authorId", book.getAuthor().getId()
        );

        int updated = jdbc.update(sql, params);
        if (updated == 0) {
            throw new EntityNotFoundException("Book with id %d not found".formatted(book.getId()));
        }

        removeGenresRelationsFor(book);
        batchInsertGenresRelationsFor(book);

        return book;
    }

    private void batchInsertGenresRelationsFor(Book book) {
        var genres = book.getGenres();
        if (Objects.isNull(genres) || genres.isEmpty()) {
            return;
        }

        var sql = "insert into books_genres(book_id, genre_id) values (:bookId, :genreId)";

        MapSqlParameterSource[] batchParams = genres.stream()
                .map(genre -> new MapSqlParameterSource()
                        .addValue("bookId", book.getId())
                        .addValue("genreId", genre.getId()))
                .toArray(MapSqlParameterSource[]::new);

        jdbc.batchUpdate(sql, batchParams);
    }

    private void removeGenresRelationsFor(Book book) {
        var sql = "delete from books_genres where book_id = :bookId";
        jdbc.update(sql, Map.of("bookId", book.getId()));
    }

    private static class BookRowMapper implements RowMapper<Book> {

        @Override
        public Book mapRow(ResultSet rs, int rowNum) throws SQLException {
            long id = rs.getLong("id");
            String title = rs.getString("title");

            long authorId = rs.getLong("author_id");
            String authorName = rs.getString("author_full_name");
            Author author = new Author(authorId, authorName);

            return new Book(id, title, author, new ArrayList<>());
        }
    }


    @SuppressWarnings("ClassCanBeRecord")
    @RequiredArgsConstructor
    private static class BookResultSetExtractor implements ResultSetExtractor<Book> {

        @Override
        public Book extractData(ResultSet rs) throws SQLException, DataAccessException {
            Book book = null;
            List<Genre> genres = new ArrayList<>();

            while (rs.next()) {
                if (Objects.isNull(book)) {
                    long bookId = rs.getLong("book_id");
                    String title = rs.getString("title");

                    long authorId = rs.getLong("author_id");
                    String authorName = rs.getString("author_full_name");
                    Author author = new Author(authorId, authorName);

                    book = new Book(bookId, title, author, genres);
                }

                long genreId = rs.getLong("genre_id");
                if (!rs.wasNull()) {
                    String genreName = rs.getString("genre_name");
                    genres.add(new Genre(genreId, genreName));
                }
            }

            return book;
        }
    }

    private record BookGenreRelation(long bookId, long genreId) {
    }
}
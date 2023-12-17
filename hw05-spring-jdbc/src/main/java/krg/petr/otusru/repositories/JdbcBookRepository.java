package krg.petr.otusru.repositories;

import krg.petr.otusru.exceptions.EntityNotFoundException;
import krg.petr.otusru.models.Author;
import krg.petr.otusru.models.Book;
import krg.petr.otusru.models.Genre;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcBookRepository implements BookRepository {

    private final NamedParameterJdbcOperations namedParameterJdbcOperations;

    private final GenreRepository genreRepository;

    @Override
    public Optional<Book> findById(long id) {
        var result = namedParameterJdbcOperations.query(
                "SELECT b.id , b.title, b.author_id, a.full_name, g.id AS g_id, g.name " +
                        "FROM books b " +
                        "JOIN authors a ON b.author_id = a.id " +
                        "LEFT JOIN books_genres bg ON b.id = bg.book_id " +
                        "LEFT JOIN genres g ON g.id = bg.genre_id " +
                        "WHERE b.id = :id",
                Collections.singletonMap("id", id),
                new BookResultSetExtractor()
        );

        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public List<Book> findAll() {
        var genres = genreRepository.findAll();
        var relations = getAllGenreRelations();
        var books = getAllBooksWithoutGenres();
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
        Optional<Book> bookOptional = findById(id);
        if (bookOptional.isPresent()) {
            Book book = bookOptional.get();
            namedParameterJdbcOperations.update(
                    "DELETE FROM books WHERE id = :id",
                    Collections.singletonMap("id", id)
            );
            removeGenresRelationsFor(book);
        }
    }

    private List<Book> getAllBooksWithoutGenres() {
        return namedParameterJdbcOperations.query(
                "SELECT b.id, b.title, b.author_id, a.full_name " +
                        "FROM books b " +
                        "JOIN authors a ON b.author_id = a.id",
                new BookResultSetExtractor()
        );
    }

    private List<BookGenreRelation> getAllGenreRelations() {
        return namedParameterJdbcOperations.query(
                "SELECT book_id, genre_id " +
                        "FROM books_genres",
                new BookGenreRelationExtractor()
        );
    }

    private void mergeBooksInfo(List<Book> booksWithoutGenres, List<Genre> genres,
                                List<BookGenreRelation> relations) {
        Map<Long, List<Long>> relationsMap = new HashMap<>();
        for (BookGenreRelation relation : relations) {
            relationsMap.computeIfAbsent(relation.bookId(), k ->
                    new ArrayList<>()).add(relation.genreId());
        }

        Map<Long, Book> bookMap = new HashMap<>();
        for (Book book : booksWithoutGenres) {
            bookMap.put(book.getId(), book);
        }

        Map<Long, Genre> genreMap = new HashMap<>();
        for (Genre genre : genres) {
            genreMap.put(genre.getId(), genre);
        }

        for (Map.Entry<Long, List<Long>> entry : relationsMap.entrySet()) {
            List<Genre> genresOfBook = new ArrayList<>();
            for (Long genreId : entry.getValue()) {
                genresOfBook.add(genreMap.get(genreId));
            }
            bookMap.get(entry.getKey()).setGenres(genresOfBook);
        }
    }

    private Book insert(Book book) {
        var keyHolder = new GeneratedKeyHolder();

        MapSqlParameterSource params = new MapSqlParameterSource(
                Map.of("title", book.getTitle(),
                       "author_id", book.getAuthor().getId()
                ));

        namedParameterJdbcOperations.update(
                "INSERT INTO books(title, author_id) " +
                        "VALUES(:title, :author_id) ",
                params, keyHolder, new String[]{"id"});

        //noinspection DataFlowIssue
        book.setId(keyHolder.getKeyAs(Long.class));
        batchInsertGenresRelationsFor(book);
        return book;
    }

    private Book update(Book book) {
        SqlParameterSource params = new MapSqlParameterSource(
                Map.of("id", book.getId(),
                       "title", book.getTitle(),
                       "author_id", book.getAuthor().getId()
        ));

        int rowsUpdate =  namedParameterJdbcOperations.update(
                "UPDATE books SET title = :title, author_id = :author_id " +
                        "WHERE id = :id",
                params);

        if (rowsUpdate == 0) {
            throw new EntityNotFoundException("Entity in books table with id %d not found"
                    .formatted(book.getId()));
        }

        removeGenresRelationsFor(book);
        batchInsertGenresRelationsFor(book);

        return book;
    }

    private void batchInsertGenresRelationsFor(Book book) {
        if (book.getGenres().isEmpty()) {
            return;
        }

        List<BookGenreRelation> relations = new ArrayList<>();

        for (Genre genre : book.getGenres()) {
            relations.add(new BookGenreRelation(book.getId(), genre.getId()));
        }

        namedParameterJdbcOperations.batchUpdate(
                "INSERT INTO books_genres SET book_id = :bookId, genre_id = :genreId",
                SqlParameterSourceUtils.createBatch(relations));
    }

    private void removeGenresRelationsFor(Book book) {
        namedParameterJdbcOperations.update(
                "DELETE FROM books_genres WHERE book_id = :id",
                Collections.singletonMap("id", book.getId())
        );
    }

    @SuppressWarnings("ClassCanBeRecord")
    @RequiredArgsConstructor
    private static class BookResultSetExtractor implements ResultSetExtractor<List<Book>> {

        @Override
        public List<Book> extractData(ResultSet rs) throws SQLException, DataAccessException {
            Map<Long, Book> books = new HashMap<>();
            boolean isGenreId = verifyResultSetColumn(rs, "g_id");
            while (rs.next()) {
                long id = rs.getLong("id");
                Book book = books.get(id);

                if (book == null) {
                    long authorId = rs.getLong("author_id");
                    String authorName = rs.getString("full_name");
                    Author author = new Author(authorId, authorName);
                    String title = rs.getString("title");
                    List<Genre> genres = new ArrayList<>();

                    book = new Book(id, title, author, genres);
                    books.put(id, book);
                }
                if (isGenreId) {
                    long genreId = rs.getLong("g_id");
                    String genreName = rs.getString("name");
                    book.getGenres().add(new Genre(genreId, genreName));
                }
            }
            return new ArrayList<>(books.values());
        }

        private boolean verifyResultSetColumn(ResultSet rs, String nameColumn) throws SQLException {
            ResultSetMetaData rsMetaData = rs.getMetaData();
            int countColumns = rsMetaData.getColumnCount();

            for (int i = 1; i < countColumns + 1; i++) {
                String mdColumnName = rsMetaData.getColumnName(i);
                String mdColumnLabel = rsMetaData.getColumnLabel(i);

                if (nameColumn.toUpperCase().equals(mdColumnName) ||
                        nameColumn.toUpperCase().equals(mdColumnLabel)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static class BookGenreRelationExtractor implements ResultSetExtractor<List<BookGenreRelation>> {
        @Override
        public List<BookGenreRelation> extractData(ResultSet rs) throws SQLException, DataAccessException {
            List<BookGenreRelation> list = new ArrayList<>();
            while (rs.next()) {
                list.add(new BookGenreRelation(rs.getLong("book_id"), rs.getLong("genre_id")));
            }
            return list;
        }
    }

    private record BookGenreRelation(long bookId, long genreId) {
    }
}
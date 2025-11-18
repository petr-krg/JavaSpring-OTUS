package krg.petr.otusru.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import krg.petr.otusru.models.Author;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcAuthorRepository implements AuthorRepository {

    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public List<Author> findAll() {
        var sql = """
                select id, full_name
                from authors
                order by id
                """;
        return jdbc.query(sql, new AuthorRowMapper());
    }

    @Override
    public Optional<Author> findById(long id) {
        var sql = """
                select id, full_name
                from authors
                where id = :id
                """;
        var params = Map.of("id", id);
        var authors = jdbc.query(sql, params, new AuthorRowMapper());
        return authors.stream().findFirst();
    }

    private static class AuthorRowMapper implements RowMapper<Author> {

        @Override
        public Author mapRow(ResultSet rs, int i) throws SQLException {
            return new Author(
                    rs.getLong("id"),
                    rs.getString("full_name")
            );
        }
    }
}
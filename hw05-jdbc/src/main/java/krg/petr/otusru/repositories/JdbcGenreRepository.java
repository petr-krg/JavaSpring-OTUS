package krg.petr.otusru.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import krg.petr.otusru.models.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class JdbcGenreRepository implements GenreRepository {

    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public List<Genre> findAll() {
        var sql = """
                select id, name
                from genres
                order by id
                """;
        return jdbc.query(sql, new GnreRowMapper());
    }

    @Override
    public List<Genre> findAllByIds(Set<Long> ids) {
        if (Objects.isNull(ids) || ids.isEmpty()) {
            return List.of();
        }

        var sql = """
                select id, name
                from genres
                where id in (:ids)
                order by id
                """;
        var params = Map.of("ids", ids);
        return jdbc.query(sql, params, new GnreRowMapper());
    }

    private static class GnreRowMapper implements RowMapper<Genre> {

        @Override
        public Genre mapRow(ResultSet rs, int i) throws SQLException {
            return new Genre(
                    rs.getLong("id"),
                    rs.getString("name")
            );
        }
    }
}
package krg.petr.otusru.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import krg.petr.otusru.models.Genre;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Repository
public class JpaGenreRepository implements GenreRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Genre> findAll() {
        return entityManager.createQuery(
                "select g from Genre g order by g.id", Genre.class)
                .getResultList();
    }

    @Override
    public List<Genre> findAllByIds(Set<Long> ids) {
        if (Objects.isNull(ids) || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return entityManager.createQuery(
                        "select g from Genre g where g.id in :ids order by g.id", Genre.class)
                .setParameter("ids", ids)
                .getResultList();
    }
}
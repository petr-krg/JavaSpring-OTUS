package krg.petr.otusru.repositories.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import krg.petr.otusru.models.Genre;
import krg.petr.otusru.repositories.GenreRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public class GenreRepositoryImpl implements GenreRepository {

    @PersistenceContext
    private final EntityManager entityManager;

    public GenreRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<Genre> findAll() {
        return entityManager.createQuery(
                "SELECT g FROM Genre g", Genre.class)
                .getResultList();
    }

    @Override
    public List<Genre> findAllByIds(Set<Long> ids) {
        TypedQuery<Genre> query = entityManager.createQuery(
                "SELECT g FROM Genre g WHERE g.id in (:ids)", Genre.class);
        query.setParameter("ids", ids);
        return query.getResultList();
    }

}
package krg.petr.otusru.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import krg.petr.otusru.exceptions.EntityNotFoundException;
import krg.petr.otusru.models.Book;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class JpaBookRepository implements BookRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Book> findById(long id) {
        TypedQuery<Book> query = entityManager.createQuery(
                "select distinct b from Book b " +
                        "join fetch b.author " +
                        "left join fetch b.genres " +
                        "where b.id = :id", Book.class);
        query.setParameter("id", id);
        List<Book> result = query.getResultList();
        return result.stream().findFirst();
    }

    @Override
    public List<Book> findAll() {
        return entityManager.createQuery(
                "select distinct b from Book b " +
                        "join fetch b.author " +
                        "left join fetch b.genres " +
                        "order by b.id", Book.class)
                .getResultList();
    }

    @Override
    public Book save(Book book) {
        if (book.getId() == 0) {
            entityManager.persist(book);
            return book;
        } else {
            Book merged = entityManager.merge(book);
            if (Objects.isNull(merged)) {
                throw new EntityNotFoundException("Book with id %d not found".formatted(book.getId()));
            }
            return merged;
        }
    }

    @Override
    public void deleteById(long id) {
        Book book = entityManager.find(Book.class, id);
        if (Objects.nonNull(book)) {
            entityManager.remove(book);
        }
    }
}
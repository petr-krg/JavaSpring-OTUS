package krg.petr.otusru.repositories.impl;

import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import krg.petr.otusru.models.Book;
import krg.petr.otusru.models.Genre;
import krg.petr.otusru.repositories.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BookRepositoryImpl implements BookRepository {

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public Optional<Book> findById(long id) {
        EntityGraph<?> entityGraph = entityManager.getEntityGraph("book-author-genres-graph");

        Book book = entityManager.find(Book.class, id,
                Collections.singletonMap("javax.persistence.fetchgraph", entityGraph));

        return Optional.ofNullable(book);
    }

    @Override
    public List<Book> findAll() {
        EntityGraph<?> entityGraph = entityManager.getEntityGraph("book-with-author");

        return entityManager.createQuery("SELECT b FROM Book b", Book.class)
                .setHint("javax.persistence.fetchgraph", entityGraph)
                .getResultList();
    }

    @Override
    public Book save(Book book) {
        List<Genre> mergedGenres = book.getGenres().stream()
                .map(entityManager::merge)
                .toList();
        book.setGenres(mergedGenres);

        if (book.getId() != null && book.getId() > 0) {
            return update(book);
        }
        return insert(book);
    }

    @Override
    public void deleteById(long id) {
        entityManager.remove(entityManager.find(Book.class, id));
    }

    private Book update(Book book) {
        entityManager.merge(book);
        return book;
    }

    private Book insert(Book book) {
        entityManager.persist(book);
        return book;
    }
}
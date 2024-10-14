package krg.petr.otusru.repositories.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import krg.petr.otusru.models.Book;
import krg.petr.otusru.models.Comment;
import krg.petr.otusru.repositories.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepository {

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public Optional<Comment> findById(long id) {
        EntityGraph<?> entityGraph = entityManager.getEntityGraph("comment-with-book");

        return Optional.ofNullable(entityManager.find(Comment.class, id,
                Collections.singletonMap("javax.persistence.fetchgraph", entityGraph)));
    }

    @Override
    public List<Comment> findByBookId(long id) {
        Book book = entityManager.find(Book.class, id);

        if (book == null) {
            return Collections.emptyList();
        }

        TypedQuery<Comment> query = entityManager.createQuery(
                "SELECT c FROM Comment c " +
                        "WHERE c.book.id = :id", Comment.class);
        query.setParameter("id", id);
        return query.getResultList();
    }

    @Override
    public Comment save(Comment comment) {
        if (comment.getId() != null && comment.getId() > 0) {
            return update(comment);
        }
        return insert(comment);
    }

    @Override
    public void delete(Comment comment) {
        entityManager.remove(comment);
    }

    private Comment update(Comment comment) {
        return entityManager.merge(comment);
    }

    private Comment insert(Comment comment) {
        entityManager.persist(comment);
        return comment;
    }
}
package krg.petr.otusru.repositories.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import krg.petr.otusru.models.Comment;
import krg.petr.otusru.repositories.CommentRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CommentRepositoryImpl implements CommentRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Comment> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Comment.class, id));
    }

    @Override
    public List<Comment> findByBookId(Long id) {
        TypedQuery<Comment> query = entityManager.createQuery(
                "SELECT c FROM Comment c " +
                   "   JOIN FETCH c.book " +
                   "WHERE c.book.id = :id", Comment.class);
        query.setParameter("id", id);
        return query.getResultList();
    }

    @Override
    public Comment save(Comment comment) {
        if (comment.getId() == 0) {
            return insert(comment);
        }
        return update(comment);
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
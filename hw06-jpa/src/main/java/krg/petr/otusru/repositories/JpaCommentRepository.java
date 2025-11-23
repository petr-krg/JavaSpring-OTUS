package krg.petr.otusru.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import krg.petr.otusru.models.Comment;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class JpaCommentRepository implements CommentRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Comment> findById(long id) {
        var result = entityManager.createQuery(
                        "select c from Comment c " +
                                "join fetch c.book b " +
                                "where c.id = :id", Comment.class)
                .setParameter("id", id)
                .getResultList();
        return result.stream().findFirst();
    }

    @Override
    public List<Comment> findAll() {
        return entityManager.createQuery(
                "select c from Comment c " +
                        "join fetch c.book b " +
                        "order by c.id", Comment.class)
                .getResultList();
    }

    @Override
    public List<Comment> findByBookId(long bookId) {
        return entityManager.createQuery(
                "select c from Comment c " +
                        "join fetch c.book b " +
                        "where b.id = :bookId " +
                        "order by c.id", Comment.class)
                .setParameter("bookId", bookId)
                .getResultList();
    }

    @Override
    public Comment save(Comment comment) {
        if (comment.getId() == 0) {
            entityManager.persist(comment);
            return comment;
        } else {
            return entityManager.merge(comment);
        }
    }

    @Override
    public void deleteById(long id) {
        Comment comment = entityManager.find(Comment.class, id);
        if (Objects.nonNull(comment)) {
            entityManager.remove(comment);
        }
    }
}
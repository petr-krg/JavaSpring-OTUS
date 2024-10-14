package krg.petr.otusru.repositories;

import krg.petr.otusru.models.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepository {

    Optional<Comment> findById(long id);

    List<Comment> findByBookId(long id);

    Comment save(Comment comment);

    void delete(Comment comment);
}
package krg.petr.otusru.services;

import krg.petr.otusru.models.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentService {

    List<Comment> findAll();

    Optional<Comment> findById(long id);

    List<Comment> findByBookId(long bookId);

    Comment create(long bookId, String text);

    Comment update(long id, long bookId, String text);

    void deleteById(long id);
}
package krg.petr.otusru.services;

import krg.petr.otusru.models.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentService {

    Optional<Comment> findById(Long id);

    List<Comment> findByBookId(Long id);

    Comment insert(long bookId, String text);

    Comment update(long id, String text);

    void deleteById(long id);
}
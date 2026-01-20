package krg.petr.otusru.services;

import krg.petr.otusru.converters.CommentConverter;
import krg.petr.otusru.exceptions.EntityNotFoundException;
import krg.petr.otusru.models.Book;
import krg.petr.otusru.models.Comment;
import krg.petr.otusru.repositories.BookRepository;
import krg.petr.otusru.repositories.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    private final BookRepository bookRepository;

    private final CommentConverter commentConverter;

    @Override
    @Transactional(readOnly = true)
    public String findAll() {
        return commentRepository.findAll().stream()
                .map(commentConverter::commentToString)
                .collect(Collectors.joining("," + System.lineSeparator()));
    }

    @Override
    @Transactional(readOnly = true)
    public String findById(long id) {
        return commentRepository.findById(id)
                .map(commentConverter::commentToString)
                .orElse("Comment with id %d not found".formatted(id));
    }

    @Override
    @Transactional(readOnly = true)
    public String findByBookId(long bookId) {
        var comments = commentRepository.findByBookId(bookId);

        if (comments.isEmpty()) {
            return "No comments for book with id %d".formatted(bookId);
        }

        return comments.stream()
                .map(commentConverter::commentToString)
                .collect(Collectors.joining("," + System.lineSeparator()));
    }

    @Override
    @Transactional
    public String merge(long id, long bookId, String text) {
        Comment comment = save(id, bookId, text);
        return commentConverter.commentToString(comment);
    }

    private Comment save(long id, long bookId, String text) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book with id %d not found".formatted(bookId)));
        Comment comment = new Comment(id, text, book);
        return commentRepository.save(comment);
    }

    @Override
    @Transactional
    public void deleteById(long id) {
        commentRepository.deleteById(id);
    }
}
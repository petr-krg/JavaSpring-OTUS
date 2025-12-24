package krg.petr.otusru.commands;

import krg.petr.otusru.converters.CommentConverter;
import krg.petr.otusru.services.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.util.stream.Collectors;

@ShellComponent
@RequiredArgsConstructor
public class CommentCommands {

    private final CommentService commentService;

    private final CommentConverter commentConverter;

    // acm
    @ShellMethod(value = "Find all comments", key = "acm")
    public String findAllComments() {
        return commentService.findAll().stream()
                .map(commentConverter::commentToString)
                .collect(Collectors.joining("," + System.lineSeparator()));
    }

    // cmid 1
    @ShellMethod(value = "Find comment by id", key = "cmid")
    public String findCommentById(long id) {
        return commentService.findById(id)
                .map(commentConverter::commentToString)
                .orElse("Comment with id %d not found".formatted(id));
    }

    // cmbid 1
    @ShellMethod(value = "Find comments by book id", key = "cmbid")
    public String findCommentsByBookId(long bookId) {
        var comments = commentService.findByBookId(bookId);

        if (comments.isEmpty()) {
            return "No comments for book with id %d".formatted(bookId);
        }

        return comments.stream()
                .map(commentConverter::commentToString)
                .collect(Collectors.joining("," + System.lineSeparator()));
    }

    // cins 1 "Новый кометарий"
    @ShellMethod(value = "Insert comment", key = "cins")
    public String insertComment(long bookId, String text) {
        var saved = commentService.create(bookId, text);
        return commentConverter.commentToString(saved);
    }

    // cupd 2 1 "Отредактированный комментарий"
    @ShellMethod(value = "Update comment", key = "cupd")
    public String updateComment(long id, long bookId, String text) {
        var saved = commentService.update(id, bookId, text);
        return commentConverter.commentToString(saved);
    }

    // cdel 2
    @ShellMethod(value = "Delete comment by id", key = "cdel")
    public void deleteComment(long id) {
        commentService.deleteById(id);
    }
}
package krg.petr.otusru.commands;

import krg.petr.otusru.services.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@SuppressWarnings({"SpellCheckingInspection", "unused"})
@ShellComponent
@RequiredArgsConstructor
public class CommentCommands {

    private final CommentService commentService;

    // acm
    @ShellMethod(value = "Find all comments", key = "acm")
    public String findAllComments() {
        return commentService.findAll();
    }

    // cmid 1
    @ShellMethod(value = "Find comment by id", key = "cmid")
    public String findCommentById(long id) {
        return commentService.findById(id);
    }

    // cmbid 1
    @ShellMethod(value = "Find comments by book id", key = "cmbid")
    public String findCommentsByBookId(long bookId) {
        return commentService.findByBookId(bookId);
    }

    // cins 1 "Новый кометарий"
    @ShellMethod(value = "Insert comment", key = "cins")
    public String insertComment(long bookId, String text) {
        return commentService.merge(0, bookId, text);
    }

    // cupd 2 1 "Отредактированный комментарий"
    @ShellMethod(value = "Update comment", key = "cupd")
    public String updateComment(long id, long bookId, String text) {
        return commentService.merge(id, bookId, text);
    }

    // cdel 2
    @ShellMethod(value = "Delete comment by id", key = "cdel")
    public void deleteComment(long id) {
        commentService.deleteById(id);
    }
}
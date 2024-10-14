package krg.petr.otusru.services;


import krg.petr.otusru.exceptions.EntityNotFoundException;
import krg.petr.otusru.models.Comment;
import krg.petr.otusru.repositories.impl.BookRepositoryImpl;
import krg.petr.otusru.repositories.impl.CommentRepositoryImpl;
import krg.petr.otusru.services.impl.CommentServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Тесты сервиса для работы с комментариями")
@DataJpaTest
@Import({CommentServiceImpl.class, CommentRepositoryImpl.class, BookRepositoryImpl.class})
@Transactional(propagation = Propagation.NEVER)
public class CommentServiceImplTest {

    private static final Long FIRST_ID = 1L;

    private static final Long SECOND_ID = 2L;

    private static final int COUNT_COMMENTS_FIRST_ID = 2;

    @Autowired
    private CommentServiceImpl commentService;

    @DisplayName("должен загружать комментарий по id")
    @Test
    void shouldReturnCorrectCommentById() {
        var actualComment = commentService.findById(FIRST_ID);

        assertThat(actualComment).isPresent();
        assertThat(actualComment.get().getId()).isEqualTo(FIRST_ID);
        assertThat(actualComment.get().getBook()).isNotNull();
    }

    @DisplayName("должен загружать комментарий по id книги")
    @Test
    void shouldReturnCorrectCommentByBookId() {
        var actualComments = commentService.findByBookId(FIRST_ID);

        assertEquals(FIRST_ID, actualComments.get(0).getBook().getId());
        assertThat(actualComments).hasSize(COUNT_COMMENTS_FIRST_ID);
    }

    @DisplayName("должен сохранять новый комментарий")
    @Test
    void shouldSaveNewComment() {
        String newTextComment = "New Comment";
        commentService.insert(SECOND_ID, newTextComment);
        List<Comment> comments = commentService.findByBookId(SECOND_ID);

        Comment newComment = comments.stream()
                .filter(comment -> comment.getText().equals(newTextComment))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Comment not found"));

        assertDoesNotThrow(() -> newComment.getBook().getId());
    }

    @DisplayName("должен сохранять измененный комментарий")
    @Test
    void shouldSaveUpdatedComment() {
        String updateTextComment = "Update Comment";
        var expectedComment = commentService.findById(FIRST_ID);

        assertThat(expectedComment)
                .isPresent()
                .hasValueSatisfying(comment -> assertThat(comment.getText()).isNotEqualTo(updateTextComment));

        commentService.update(FIRST_ID, updateTextComment);
        var actualComment = commentService.findById(FIRST_ID);

        assertThat(actualComment)
                .isPresent()
                .hasValueSatisfying(comment -> {
                    assertThat(comment.getText()).isEqualTo(updateTextComment);
                    assertThat(comment.getBook()).isNotNull();
                    assertThat(comment.getBook().getId()).isEqualTo(FIRST_ID);
                });
    }

    @DisplayName("должен удалять комментарий по id ")
    @Test
    void shouldDeleteComment() {
        var expectedComment = commentService.findById(3L);
        assertThat(expectedComment).isNotNull();

        commentService.deleteById(3L);
        var deleteComment = commentService.findById(3L);
        assertThat(deleteComment).isEmpty();
    }
}
package krg.petr.otusru.services;

import krg.petr.otusru.converters.CommentConverter;
import krg.petr.otusru.exceptions.EntityNotFoundException;
import krg.petr.otusru.models.Comment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

@DataJpaTest
@Import({CommentServiceImpl.class})
@DisplayName("Тест сервиса работы с комментариями")
public class CommentServiceImplTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private TestEntityManager em;

    @MockitoBean
    private CommentConverter commentConverter;

    @BeforeEach
    void setUp() {
        when(commentConverter.commentToString(any(Comment.class)))
                .thenAnswer(invocation -> {
                    Comment comment = invocation.getArgument(0);
                    return "Comment: id=%d, text=%s".formatted(comment.getId(), comment.getText());
                });
    }

    @Test
    @DisplayName("Загружаем комментарий по id")
    void shouldReturnCorrectCommentById() {
        String result = commentService.findById(1L);

        assertThat(result).contains("Comment: id=1");
    }

    @Test
    @DisplayName("Возвращаем сообщение при отсутствии комментария")
    void shouldReturnMessageWhenCommentNotFound() {
        String result = commentService.findById(999L);

        assertThat(result).isEqualTo("Comment with id 999 not found");
    }

    @Test
    @DisplayName("Загружаем список всех комментариев")
    void shouldReturnCorrectCommentsList() {
        String result = commentService.findAll();

        assertThat(result).contains("Comment: id=1");
        assertThat(result).contains("Comment: id=2");
    }

    @Test
    @DisplayName("Загружаем комментарии по id книги")
    void shouldReturnCommentsByBookId() {
        String result = commentService.findByBookId(1L);

        assertThat(result).contains("Comment:");
    }

    @Test
    @DisplayName("должен возвращать сообщение при отсутствии комментариев у книги")
    void shouldReturnMessageWhenNoCommentsForBook() {
        String result = commentService.findByBookId(999L);

        assertThat(result).isEqualTo("No comments for book with id 999");
    }

    @Test
    @DisplayName("должен сохранять новый комментарий")
    void shouldSaveNewComment() {
        String result = commentService.merge(0L, 1L, "New comment text");

        assertThat(result).contains("New comment text");

        var comments = em.getEntityManager()
                .createQuery("SELECT c FROM Comment c WHERE c.text = :text", Comment.class)
                .setParameter("text", "New comment text")
                .getResultList();

        assertThat(comments).hasSize(1);
        var savedComment = comments.get(0);
        assertThat(savedComment.getText()).isEqualTo("New comment text");
        assertThat(savedComment.getBook().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Сохраняем измененный комментарий")
    void shouldSaveUpdatedComment() {
        String result = commentService.merge(1L, 1L, "Updated comment text");

        assertThat(result).contains("Updated comment text");

        var commentFromDb = em.find(Comment.class, 1L);

        assertThat(commentFromDb).isNotNull();
        assertThat(commentFromDb.getText()).isEqualTo("Updated comment text");
        assertThat(commentFromDb.getBook().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("должен выбрасывать исключение при отсутсвие книге")
    void shouldThrowExceptionWhenBookNotFound() {
        assertThatThrownBy(() -> commentService.merge(0L, 999L, "New comment"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Book with id 999 not found");
    }

    @Test
    @DisplayName("должен удалять комментарий по id")
    void shouldDeleteComment() {
        assertThat(em.find(Comment.class, 1L)).isNotNull();

        commentService.deleteById(1L);

        em.flush();
        em.clear();
        assertThat(em.find(Comment.class, 1L)).isNull();
    }
}
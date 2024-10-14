package krg.petr.otusru.repositories;

import krg.petr.otusru.models.Book;
import krg.petr.otusru.models.Comment;
import krg.petr.otusru.repositories.impl.CommentRepositoryImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;


@DisplayName("Репозиторий на основе JPA для работы с комментариями к книгам ")
@DataJpaTest
@Import({CommentRepositoryImpl.class})
public class CommentRepositoryTest {

    private final Long COMMENT_ID = 1L;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CommentRepository commentRepository;

    @DisplayName("должен загружать комментарий по id")
    @Test
    void shouldReturnCorrectCommentById() {
        var expectedComment = entityManager.find(Comment.class, COMMENT_ID);
        var actualComment = commentRepository.findById(expectedComment.getId());

        assertThat(actualComment)
                .isPresent()
                .get()
                .isEqualTo(expectedComment);
    }

    @DisplayName("должен сохранять измененный комментарий")
    @Test
    void shouldSaveUpdatedComment() {
        var expectedComment = entityManager.find(Comment.class, COMMENT_ID);
        assertThat(expectedComment).isNotNull();

        var updateComment = new Comment(COMMENT_ID, "update comment",
                entityManager.find(Book.class, 1L));

        assertThat(expectedComment.getText())
                .isNotEqualTo(updateComment.getText());

        commentRepository.save(updateComment);
        entityManager.flush();
        entityManager.clear();

        var saveComment = entityManager.find(Comment.class, COMMENT_ID);
        assertThat(saveComment.getText())
                .isEqualTo(updateComment.getText());
    }

    @DisplayName("должен удалять комментарий по id ")
    @Test
    void shouldDeleteComment() {
        var expectedComment = entityManager.find(Comment.class, COMMENT_ID);
        assertThat(expectedComment).isNotNull();

        commentRepository.delete(expectedComment);
        entityManager.flush();
        entityManager.clear();

        var deleteComment = entityManager.find(Comment.class, COMMENT_ID);
        assertThat(deleteComment).isNull();
    }


}
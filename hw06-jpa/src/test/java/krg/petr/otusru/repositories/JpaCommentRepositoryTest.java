package krg.petr.otusru.repositories;

import krg.petr.otusru.models.Book;
import krg.petr.otusru.models.Comment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@DisplayName("Репозиторий на основе jpa для работы с комментариями")
@DataJpaTest
@Import(JpaCommentRepository.class)
public class JpaCommentRepositoryTest {

    @Autowired
    private JpaCommentRepository commentRepository;

    @Autowired
    private TestEntityManager entityManager;

    @DisplayName("должен загружать список всех комментариев")
    @Test
    void shouldReturnAllComments() {
        List<Comment> comments = commentRepository.findAll();

        assertThat(comments).hasSize(4);

        assertThat(comments)
                .extracting(
                        Comment::getId,
                        Comment::getText,
                        c -> c.getBook().getId()
                )
                .containsExactlyInAnyOrder(
                        tuple(1L, "Comment_1 for Book_1", 1L),
                        tuple(2L, "Comment_2 for Book_1", 1L),
                        tuple(3L, "Comment_1 for Book_2", 2L),
                        tuple(4L, "Comment_1 for Book_3", 3L)
                );
    }

    @DisplayName("должен загружать комментарий по id")
    @Test
    void shouldReturnCommentById() {
        var commentOpt = commentRepository.findById(1L);

        assertThat(commentOpt).isPresent();
        var comment = commentOpt.get();

        assertThat(comment.getId()).isEqualTo(1L);
        assertThat(comment.getText()).isEqualTo("Comment_1 for Book_1");
        assertThat(comment.getBook().getId()).isEqualTo(1L);
    }

    @DisplayName("должен загружать комментарии по id книги")
    @Test
    void shouldReturnCommentsByBookId() {
        List<Comment> comments = commentRepository.findByBookId(1L);

        assertThat(comments).hasSize(2);

        assertThat(comments)
                .extracting(
                        Comment::getText,
                        c -> c.getBook().getId()
                )
                .containsExactlyInAnyOrder(
                        tuple("Comment_1 for Book_1", 1L),
                        tuple("Comment_2 for Book_1", 1L)
                );
    }

    @DisplayName("должен сохранять новый комментарий")
    @Test
    void shouldSaveNewComment() {
        Book book = entityManager.find(Book.class, 1L);
        Comment newComment = new Comment(0L, "New comment for Book_1", book);
        Comment saved = commentRepository.save(newComment);

        assertThat(saved.getId()).isGreaterThan(0);
        var fromDbOpt = commentRepository.findById(saved.getId());

        assertThat(fromDbOpt).isPresent();
        var fromDb = fromDbOpt.get();

        assertThat(fromDb.getText()).isEqualTo("New comment for Book_1");
        assertThat(fromDb.getBook().getId()).isEqualTo(1L);
    }

    @DisplayName("должен удалять комментарий по id")
    @Test
    void shouldDeleteCommentById() {
        assertThat(commentRepository.findById(1L)).isPresent();
        commentRepository.deleteById(1L);
        assertThat(commentRepository.findById(1L)).isEmpty();
    }
}
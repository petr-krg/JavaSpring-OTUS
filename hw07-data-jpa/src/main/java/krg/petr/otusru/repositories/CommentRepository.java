package krg.petr.otusru.repositories;

import krg.petr.otusru.models.Comment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Override
    @EntityGraph(attributePaths = "book")
    Optional<Comment> findById(@Param("id") Long id);

    List<Comment> findByBookId(long bookId);

}
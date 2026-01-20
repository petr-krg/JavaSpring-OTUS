package krg.petr.otusru.repositories;

import krg.petr.otusru.models.Book;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    @Override
    @EntityGraph(attributePaths = {"author"})
    List<Book> findAll();

    @Override
    @EntityGraph(attributePaths = {"author"})
    Optional<Book> findById(@Param("id") Long id);
}
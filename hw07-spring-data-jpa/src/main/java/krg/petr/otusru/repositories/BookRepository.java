package krg.petr.otusru.repositories;

import krg.petr.otusru.models.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findById(long id);

    List<Book> findAll();

}
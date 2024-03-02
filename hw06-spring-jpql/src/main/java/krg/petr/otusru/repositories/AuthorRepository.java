package krg.petr.otusru.repositories;

import krg.petr.otusru.models.Author;

import java.util.List;
import java.util.Optional;

public interface AuthorRepository {

    List<Author> findAll();

    Optional<Author> findById(long id);
}
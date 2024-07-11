package krg.petr.otusru.services;

import krg.petr.otusru.models.Author;

import java.util.List;
import java.util.Optional;

public interface AuthorService {

    List<Author> findAll();

    Optional<Author> findById(long id);
}
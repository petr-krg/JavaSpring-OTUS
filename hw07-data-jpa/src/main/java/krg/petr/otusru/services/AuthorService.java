package krg.petr.otusru.services;

import krg.petr.otusru.models.Author;

import java.util.List;

public interface AuthorService {
    List<Author> findAll();
}
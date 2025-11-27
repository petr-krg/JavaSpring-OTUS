package krg.petr.otusru.services;

import krg.petr.otusru.models.Genre;

import java.util.List;

public interface GenreService {
    List<Genre> findAll();
}
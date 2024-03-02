package krg.petr.otusru.services.impl;

import krg.petr.otusru.services.GenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import krg.petr.otusru.models.Genre;
import krg.petr.otusru.repositories.GenreRepository;

import java.util.List;

@RequiredArgsConstructor
@Service
public class GenreServiceImpl implements GenreService {
    private final GenreRepository genreRepository;

    @Override
    public List<Genre> findAll() {
        return genreRepository.findAll();
    }
}
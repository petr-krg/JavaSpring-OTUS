package krg.petr.otusru.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import krg.petr.otusru.models.Genre;
import krg.petr.otusru.repositories.GenreRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class GenreServiceImpl implements GenreService {

    private final GenreRepository genreRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Genre> findAll() {
        return genreRepository.findAll();
    }
}
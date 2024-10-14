package krg.petr.otusru.services.impl;

import krg.petr.otusru.services.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import krg.petr.otusru.models.Author;
import krg.petr.otusru.repositories.AuthorRepository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;

    @Override
    public List<Author> findAll() {
        return authorRepository.findAll();
    }

    @Override
    public Optional<Author> findById(long id) {
        return authorRepository.findById(id);
    }
}
package krg.petr.otusru.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import krg.petr.otusru.models.Author;
import krg.petr.otusru.repositories.AuthorRepository;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AuthorServiceImpl implements AuthorService {
    private final AuthorRepository authorRepository;

    @Override
    public List<Author> findAll() {
        return authorRepository.findAll();
    }
}
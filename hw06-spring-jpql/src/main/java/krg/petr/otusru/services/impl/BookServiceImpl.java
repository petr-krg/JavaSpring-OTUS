package krg.petr.otusru.services.impl;

import krg.petr.otusru.exceptions.EntityNotFoundException;
import krg.petr.otusru.models.dtos.BookDTO;
import krg.petr.otusru.services.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import krg.petr.otusru.models.Book;
import krg.petr.otusru.repositories.AuthorRepository;
import krg.petr.otusru.repositories.BookRepository;
import krg.petr.otusru.repositories.GenreRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.springframework.util.CollectionUtils.isEmpty;

@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {

    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final BookRepository bookRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Book> findById(long id) {
        return bookRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Book> findAll() {
        List<Book> books = bookRepository.findAll();
        books.forEach(book -> book.getGenres().size());
        return books;
    }

    @Override
    @Transactional
    public Book insert(String title, long authorId, Set<Long> genresIds) {
        return save(null, title, authorId, genresIds);
    }

    @Override
    @Transactional
    public Book update(long id, String title, long authorId, Set<Long> genresIds) {
        return save(id, title, authorId, genresIds);
    }

    @Override
    @Transactional
    public void deleteById(long id) {
        bookRepository.deleteById(id);
    }

    // прочитал про такой способ, решил попробовать, так сказать проба пера)
    public List<BookDTO> findAllBookDTO() {
        List<Book> books = bookRepository.findAll();
        books.forEach(book -> book.getGenres().size());

        List<BookDTO> bookDTOs = new ArrayList<>();
        books.forEach(book -> bookDTOs.add(new BookDTO(book)));

        return bookDTOs;
    }

    private Book save(Long id, String title, Long authorId, Set<Long> genresIds) {
        if (isEmpty(genresIds)) {
            throw new IllegalArgumentException("Genres ids must not be null");
        }

        var author = authorRepository.findById(authorId)
                .orElseThrow(() -> new EntityNotFoundException("Author with id %d not found".formatted(authorId)));
        var genres = genreRepository.findAllByIds(genresIds);
        if (isEmpty(genres) || genresIds.size() != genres.size()) {
            throw new EntityNotFoundException("One or all genres with ids %s not found".formatted(genresIds));
        }

        var book = new Book(id, title, author, genres);
        return bookRepository.save(book);
    }
}
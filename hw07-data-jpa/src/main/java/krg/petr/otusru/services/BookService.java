package krg.petr.otusru.services;

import java.util.Set;

public interface BookService {
    String findById(long id);

    String findAll();

    String merge(long id, String title, long authorId, Set<Long> genresIds);

    void deleteById(long id);


}
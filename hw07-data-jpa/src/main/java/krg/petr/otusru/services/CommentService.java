package krg.petr.otusru.services;

public interface CommentService {

    String findById(long id);

    String findByBookId(long bookId);

    String merge(long id, long bookId, String text);

    void deleteById(long id);
}
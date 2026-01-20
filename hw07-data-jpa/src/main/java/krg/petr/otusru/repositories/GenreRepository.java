package krg.petr.otusru.repositories;

import krg.petr.otusru.models.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {

    List<Genre> findByIdInOrderByName(Set<Long> ids);
}
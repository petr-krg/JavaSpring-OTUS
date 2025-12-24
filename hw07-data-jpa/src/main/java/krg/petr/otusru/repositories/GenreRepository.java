package krg.petr.otusru.repositories;

import krg.petr.otusru.models.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {

    @Query("select g from Genre g where g.id in :ids order by g.name")
    List<Genre> findAllByIds(Set<Long> ids);
}
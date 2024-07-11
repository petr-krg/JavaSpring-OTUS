package krg.petr.otusru.repositories;

import krg.petr.otusru.models.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface GenreRepository extends JpaRepository<Genre, Long> {

    @Query("SELECT g FROM Genre g WHERE g.id in :ids")
    List<Genre> findAllByIds(@Param("ids") Set<Long> ids);
}
package krg.petr.otusru.dao;

import krg.petr.otusru.domain.Question;

import java.util.List;

public interface QuestionDao {
    List<Question> findAll();
}

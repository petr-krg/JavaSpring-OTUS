package krg.petr.otusru.dao;

import krg.petr.otusru.config.TestFileNameProvider;
import krg.petr.otusru.domain.Answer;
import krg.petr.otusru.domain.Question;
import krg.petr.otusru.exceptions.QuestionReadException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;

import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;

@DisplayName("CsvQuestionDao загрузка CSV из test/resources")
class CsvQuestionDaoTest {

    private TestFileNameProvider fileNameProvider;

    @BeforeEach
    void setUp() {
        fileNameProvider = Mockito.mock(TestFileNameProvider.class);
    }

    @Test
    @DisplayName("findAll() читаем test/resources/questions.csv положительно")
    void findAll_readsFromTestResources() {
        given(fileNameProvider.getTestFileName()).willReturn("questions_test.csv");
        CsvQuestionDao dao = new CsvQuestionDao(fileNameProvider);
        InputStream sanity = getClass().getClassLoader().getResourceAsStream("questions_test.csv");
        assertThat(sanity).as("questions_test.csv должен находится в test classpath").isNotNull();

        List<Question> questions = dao.findAll();

        assertThat(questions)
                .as("проверяем вопросы")
                .isNotEmpty();

        assertThat(questions)
                .allSatisfy(q -> {
                    assertThat(q.getText()).isNotBlank();
                    assertThat(q.getAnswers()).isNotEmpty();
                    assertThat(q.getAnswers().stream().anyMatch(Answer::isCorrect))
                            .as("each question must have at least one correct answer")
                            .isTrue();
                });

        BDDMockito.then(fileNameProvider).should().getTestFileName();
        BDDMockito.then(fileNameProvider).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("findAll() бросаем QuestionReadException если файл не найден")
    void findAll_throwsWhenFileMissing() {
        String wrong = "no-such-file.csv";
        String errorMessage = "Could not load questions from the resource: " + wrong;
        given(fileNameProvider.getTestFileName()).willReturn(wrong);
        CsvQuestionDao dao = new CsvQuestionDao(fileNameProvider);

        assertThatThrownBy(dao::findAll)
                .isInstanceOf(QuestionReadException.class)
                .hasMessageContaining(errorMessage)
                .hasRootCauseInstanceOf(NullPointerException.class);
        BDDMockito.then(fileNameProvider).should(times(2)).getTestFileName();
        BDDMockito.then(fileNameProvider).shouldHaveNoMoreInteractions();
    }
}
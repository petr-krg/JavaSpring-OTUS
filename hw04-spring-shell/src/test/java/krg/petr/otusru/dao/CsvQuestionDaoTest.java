package krg.petr.otusru.dao;

import krg.petr.otusru.config.TestFileNameProvider;
import krg.petr.otusru.domain.Answer;
import krg.petr.otusru.domain.Question;
import krg.petr.otusru.exceptions.QuestionReadException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.atLeastOnce;

@SpringBootTest(classes = CsvQuestionDao.class)
@TestPropertySource(properties = "spring.shell.interactive.enabled=false")
@DisplayName("CsvQuestionDao загрузка CSV из test/resources")
class CsvQuestionDaoTest {

    @Autowired
    private CsvQuestionDao dao;

    @MockitoBean
    private TestFileNameProvider fileNameProvider;

    @Test
    @DisplayName("findAll() читаем test/resources/questions_test.csv положительно")
    void findAll_readsFromTestResources() {
        given(fileNameProvider.getTestFileName()).willReturn("questions_test.csv");
        try (InputStream sanity = getClass().getClassLoader().getResourceAsStream("questions_test.csv")) {
            assertThat(sanity).as("questions_test.csv должен находиться в test classpath").isNotNull();
        } catch (Exception e) {
            fail("Не удалось открыть questions_test.csv из classpath", e);
        }

        List<Question> questions = dao.findAll();

        assertThat(questions)
                .as("проверяем вопросы")
                .isNotEmpty()
                .allSatisfy(q -> {
                    assertThat(q.getText()).isNotBlank();
                    assertThat(q.getAnswers()).isNotEmpty();
                    assertThat(q.getAnswers().stream().anyMatch(Answer::isCorrect))
                            .as("каждый вопрос должен иметь хотя бы один правильный ответ")
                            .isTrue();
                });

        then(fileNameProvider).should(atLeastOnce()).getTestFileName();
        then(fileNameProvider).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("findAll() бросаем QuestionReadException если файл не найден")
    void findAll_throwsWhenFileMissing() {
        String wrong = "no-such-file.csv";
        given(fileNameProvider.getTestFileName()).willReturn(wrong);

        assertThatThrownBy(dao::findAll)
                .isInstanceOf(QuestionReadException.class)
                .hasMessageContaining(wrong);

        then(fileNameProvider).should(atLeastOnce()).getTestFileName();
        then(fileNameProvider).shouldHaveNoMoreInteractions();
    }
}
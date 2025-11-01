package krg.petr.otusru.service;

import krg.petr.otusru.dao.CsvQuestionDao;
import krg.petr.otusru.dao.QuestionDao;
import krg.petr.otusru.domain.Question;
import krg.petr.otusru.domain.Student;
import krg.petr.otusru.domain.TestResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.anyInt;
import static org.mockito.BDDMockito.anyString;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.inOrder;

@ExtendWith(MockitoExtension.class)
@DisplayName("Сервис TestServiceImpl")
class TestServiceImpTest {

    @Mock
    private QuestionDao csvQuestionDao;

    @Mock
    private IOService ioService;

    private TestServiceImpl testService;
    private Student expectedStudent;
    private List<Question> expectedQuestions;

    @BeforeEach
    void setUp() {
        expectedStudent = new Student("Joshua", "Bloch");

        try (InputStream sanity = getClass().getClassLoader()
                .getResourceAsStream("questions_test.csv")) {
            assertThat(sanity).as("questions_test.csv должен быть в test classpath").isNotNull();
        } catch (Exception e) {
            fail("Не удалось открыть questions_test.csv из test resources", e);
        }

        CsvQuestionDao loaderDao = new CsvQuestionDao(() -> "questions_test.csv");
        expectedQuestions = loaderDao.findAll();

        given(csvQuestionDao.findAll()).willReturn(expectedQuestions);
        given(ioService.readIntForRangeWithPrompt(anyInt(), anyInt(), anyString(), anyString()))
                .willReturn(1);

        testService = new TestServiceImpl(ioService, csvQuestionDao);
    }

    @Test
    @DisplayName("устанавливает студента корректно")
    void setsStudentCorrectly() {
        TestResult testResult = testService.executeTestFor(expectedStudent);
        assertThat(testResult.getStudent()).isEqualTo(expectedStudent);
        then(csvQuestionDao).should(times(1)).findAll();
        then(csvQuestionDao).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("записывает отвеченные вопросы корректно")
    void recordsAnsweredQuestions() {
        TestResult testResult = testService.executeTestFor(expectedStudent);
        assertThat(testResult.getAnsweredQuestions())
                .containsExactlyElementsOf(expectedQuestions);
    }

    @Test
    @DisplayName("считает количество правильных ответов при выборе варианта №1")
    void countsRightAnswers_whenOptionOneAlwaysChosen() {
        TestResult testResult = testService.executeTestFor(expectedStudent);
        long expectedRight = expectedQuestions.stream()
                .filter(q -> !q.getAnswers().isEmpty() && q.getAnswers().get(0).isCorrect())
                .count();
        assertThat(testResult.getRightAnswersCount()).isEqualTo((int) expectedRight);
    }

    @Test
    @DisplayName("выводим вопрос и варианты; читает ответ в нужных границах")
    void printsAndReadsWithCorrectBounds() {
        testService.executeTestFor(expectedStudent);

        InOrder inOrder = inOrder(ioService);

        inOrder.verify(ioService).printLine("");
        inOrder.verify(ioService).printFormattedLine("Please answer the questions below%n");

        Question q1 = expectedQuestions.get(0);
        inOrder.verify(ioService).printLine(q1.getText());
        for (int i = 0; i < q1.getAnswers().size(); i++) {
            inOrder.verify(ioService).printFormattedLine("%d) %s", i + 1, q1.getAnswers().get(i).getText());
        }

        ArgumentCaptor<Integer> minCap = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> maxCap = ArgumentCaptor.forClass(Integer.class);

        then(ioService).should(times(expectedQuestions.size()))
                .readIntForRangeWithPrompt(minCap.capture(), maxCap.capture(), anyString(), anyString());

        assertThat(minCap.getAllValues()).allMatch(m -> m == 1, "минимум = 1");
        assertThat(maxCap.getAllValues())
                .containsExactlyElementsOf(expectedQuestions.stream().map(q -> q.getAnswers().size()).toList());
    }
}
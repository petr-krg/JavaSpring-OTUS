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
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.anyInt;
import static org.mockito.BDDMockito.anyString;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.inOrder;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Сервис TestServiceImpl")
class TestServiceImpTest {

    @Mock
    private QuestionDao csvQuestionDao;

    @Mock
    private LocalizedIOService ioService;

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

        lenient().when(ioService.getMessage("TestService.input.answer"))
                .thenReturn("Введите номер ответа:");

        lenient().when(ioService.getMessage(eq("TestService.answer.is.invalid"), (Object[]) any()))
                .thenAnswer(inv -> {
                    Object[] args = inv.getArgument(1, Object[].class); // {min, max}
                    return java.text.MessageFormat.format(
                            "Некорректный номер. Введите число от {0} до {1}.", args);
                });

        doReturn(1).when(ioService)
                .readIntForRangeWithPrompt(anyInt(), anyInt(), anyString(), org.mockito.ArgumentMatchers.nullable(String.class));

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
                .filter(q -> !q.answers().isEmpty() && q.answers().get(0).isCorrect())
                .count();
        assertThat(testResult.getRightAnswersCount()).isEqualTo((int) expectedRight);
    }

    @Test
    @DisplayName("выводим вопрос и варианты; читает ответ в нужных границах")
    void printsAndReadsWithCorrectBounds() {
        testService.executeTestFor(expectedStudent);

        InOrder inOrder = inOrder(ioService);
        inOrder.verify(ioService).printLine("");
        inOrder.verify(ioService).printLineLocalized("TestService.answer.the.questions");
        inOrder.verify(ioService).printLine("");

        Question q1 = expectedQuestions.get(0);
        inOrder.verify(ioService).printLine(q1.text());
        for (int i = 0; i < q1.answers().size(); i++) {
            inOrder.verify(ioService).printFormattedLineLocalized(
                    "TestService.question.answer.print", i + 1, q1.answers().get(i).text());
        }

        ArgumentCaptor<Integer> minCap = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> maxCap = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<String> promptCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> errorCap  = ArgumentCaptor.forClass(String.class);

        then(ioService).should(times(expectedQuestions.size()))
                .readIntForRangeWithPrompt(minCap.capture(), maxCap.capture(),
                        promptCap.capture(), errorCap.capture());

        assertThat(minCap.getAllValues()).allMatch(v -> v == 1);
        assertThat(maxCap.getAllValues())
                .containsExactlyElementsOf(expectedQuestions.stream().map(q -> q.answers().size()).toList());

        assertThat(promptCap.getAllValues()).allSatisfy(p -> assertThat(p).isEqualTo("Введите номер ответа:"));

        for (int i = 0; i < expectedQuestions.size(); i++) {
            String err = errorCap.getAllValues().get(i);
            int max = expectedQuestions.get(i).answers().size();
            if (err != null) {
                assertThat(err).contains("1").contains(String.valueOf(max));
            }
        }
    }

    @Test
    @DisplayName("локаль RU: строки из bundles/messages_ru_RU.properties")
    void printsLocalizedPrompt_ru() {
        var ru = java.util.Locale.forLanguageTag("ru-RU");
        var bundle = java.util.ResourceBundle.getBundle("messages", ru);
        String expected = bundle.getString("TestService.input.answer");
        String errorPattern   = bundle.getString("TestService.answer.is.invalid");
        when(ioService.getMessage("TestService.input.answer"))
                .thenReturn(expected);
        when(ioService.getMessage(eq("TestService.answer.is.invalid"), anyInt(), anyInt()))
                .thenAnswer(inv -> {
                    int min = inv.getArgument(1, Integer.class);
                    int max = inv.getArgument(2, Integer.class);
                    return java.text.MessageFormat.format(errorPattern, min, max);
                });
        testService.executeTestFor(expectedStudent);
        var captor = ArgumentCaptor.forClass(String.class);
        then(ioService).should(times(expectedQuestions.size()))
                .readIntForRangeWithPrompt(anyInt(), anyInt(), captor.capture(), nullable(String.class));
        for (String p : captor.getAllValues()) {
            assertThat(p).isEqualTo(expected);
        }
    }
}
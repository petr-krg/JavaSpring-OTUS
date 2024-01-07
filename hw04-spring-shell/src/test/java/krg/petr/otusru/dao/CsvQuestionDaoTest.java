package krg.petr.otusru.dao;

import krg.petr.otusru.config.TestFileNameProvider;
import krg.petr.otusru.domain.Answer;
import krg.petr.otusru.domain.Question;
import krg.petr.otusru.exceptions.QuestionReadException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = CsvQuestionDao.class)
@DisplayName("Test CsvQuestionDao")
public class CsvQuestionDaoTest {

    @MockBean
    private TestFileNameProvider testFileProvider;
    @Autowired
    private CsvQuestionDao csvQuestionDao;
    private List<Question> expectedQuestions;

    @BeforeEach
    public void setUp() {
        expectedQuestions = new ArrayList<>();

        expectedQuestions.add(new Question("Is there life on Mars?", List.of(
                new Answer("Science doesn't know this yet", true),
                new Answer("Certainly. The red UFO is from Mars. And green is from Venus", false),
                new Answer("Absolutely not", false))));

        expectedQuestions.add(new Question("How should resources be loaded form jar in Java?", List.of(
                new Answer("ClassLoader#geResourceAsStream or ClassPathResource#getInputStream", true),
                new Answer("ClassLoader#geResource#getFile + FileReader", false),
                new Answer("Wingardium Leviosa", false))));

        expectedQuestions.add(new Question("Which option is a good way to handle the exception?", List.of(
                new Answer("@SneakyThrow", false),
                new Answer("e.printStackTrace()", false),
                new Answer("Rethrow with wrapping in business exception (for example, QuestionReadException)", true),
                new Answer("Ignoring exception", false))));

        expectedQuestions.add(new Question("In \"The X-Files\", what poster can be seen hanging" +
                        " on the wall in Mulder's office?", List.of(
                new Answer("Trust Everyone", false),
                new Answer("Search for the Truth", false),
                new Answer("I Want to Believe", true),
                new Answer("Aliens are Among Us", false))));

        expectedQuestions.add(new Question("Which of the following data types is not a primitive type in Java?",
                List.of(
                        new Answer("int", false),
                        new Answer("String", true),
                        new Answer("float", false),
                        new Answer("char", false))));

        expectedQuestions.add(new Question("What does the 'volatile' keyword in Java ensure?", List.of(
                new Answer("Class immutability", false),
                new Answer("Thread safety for a method", false),
                new Answer("Visibility of a variable across threads", true))));
        expectedQuestions.add(new Question("Which method must be implemented while using the Runnable interface?",
                List.of(
                        new Answer("execute()", false),
                        new Answer("perform()", false),
                        new Answer("start()", false),
                        new Answer("run()", true))));
    }

    @Test
    @DisplayName("Test find all, success")
    public void testFindAllSuccess() {
        when(testFileProvider.getTestFileName()).thenReturn("questions.csv");
        List<Question> actualQuestions = csvQuestionDao.findAll();
        Assertions.assertIterableEquals(expectedQuestions, actualQuestions);
    }

    @Test
    @DisplayName("Test find all, failure")
    public void testFindAllFailure() {
        when(testFileProvider.getTestFileName()).thenReturn("/nonexistent-questions.csv");
        assertThrows(QuestionReadException.class, csvQuestionDao::findAll);
    }
}
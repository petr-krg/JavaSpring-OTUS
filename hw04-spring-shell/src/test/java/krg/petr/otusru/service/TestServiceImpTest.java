package krg.petr.otusru.service;

import krg.petr.otusru.dao.QuestionDao;
import krg.petr.otusru.domain.Answer;
import krg.petr.otusru.domain.Question;
import krg.petr.otusru.domain.Student;
import krg.petr.otusru.domain.TestResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = TestServiceImpl.class)
@DisplayName("Test TestServiceImp")
public class TestServiceImpTest {

    @MockBean
    private QuestionDao csvQuestionDao;
    @MockBean
    private LocalizedIOService localizedIOService;
    @Autowired
    private TestServiceImpl testService;
    private TestResult testResult;
    private List<Question> expectedQuestions;
    private Student expectedStudent;

    @BeforeEach
    public void SetUp() {
        expectedStudent = new Student("Joshua", "Bloch");
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
                new Answer("Rethrow with wrapping in business exception (for example, QuestionReadException)",true),
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

        when(csvQuestionDao.findAll()).thenReturn(expectedQuestions);
        doReturn(1)
                .when(localizedIOService)
                .readIntForRangeWithPrompt(eq(1), eq(3), any(), any());
        doReturn(1)
                .when(localizedIOService)
                .readIntForRangeWithPrompt(eq(1), eq(4), any(), any());
        testService = new TestServiceImpl(localizedIOService, csvQuestionDao);
    }

    @Test
    @DisplayName("Test that the student is set correctly")
    public void testStudentIsSetCorrectly() {
        testResult = testService.executeTestFor(expectedStudent);
        assertEquals(testResult.getStudent(), expectedStudent);
    }

    @Test
    @DisplayName("Test that answered questions are recorded correctly")
    public void testAnsweredQuestionsAreRecorded() {
        testResult = testService.executeTestFor(expectedStudent);
        assertEquals(testResult.getAnsweredQuestions(), expectedQuestions);
    }

    @Test
    @DisplayName("Test that the right answer count is correct")
    public void testRightAnswersCount() {
        testResult = testService.executeTestFor(expectedStudent);
        assertEquals(testResult.getRightAnswersCount(), 2);
    }
}
package krg.petr.otusru;

import krg.petr.otusru.dao.QuestionDao;
import krg.petr.otusru.domain.Answer;
import krg.petr.otusru.domain.Question;
import krg.petr.otusru.service.IOService;
import krg.petr.otusru.service.TestServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("TestServiceImpl")
public class TestServiceImplTest {

    @Mock
    QuestionDao questionDao;

    @Mock
    IOService ioService;

    @InjectMocks
    TestServiceImpl testService;

    @Test
    @DisplayName("печатает заголовок и вопросы с вариантами в правильном порядке")
    void printsHeaderAndQuestionsInOrder() {
        var q1 = question("Capital of Germany?", "Berlin", "Munich", "Hamburg");
        var q2 = question("2 + 2 = ?", "3", "4");
        var questions = List.of(q1, q2);
        given(questionDao.findAll()).willReturn(questions);
        testService.executeTest();
        InOrder io = inOrder(ioService);
        io.verify(ioService).printLine("");
        io.verify(ioService).printFormattedLine("Please answer the questions below%n");

        io.verify(ioService).printLine(q1.getText());
        io.verify(ioService).printFormattedLine("%d) %s", 1, q1.getAnswers().get(0).getText());
        io.verify(ioService).printFormattedLine("%d) %s", 2, q1.getAnswers().get(1).getText());
        io.verify(ioService).printFormattedLine("%d) %s", 3, q1.getAnswers().get(2).getText());

        io.verify(ioService).printLine(q2.getText());
        io.verify(ioService).printFormattedLine("%d) %s", 1, q2.getAnswers().get(0).getText());
        io.verify(ioService).printFormattedLine("%d) %s", 2, q2.getAnswers().get(1).getText());

        verify(questionDao).findAll();
        verifyNoMoreInteractions(ioService, questionDao);
    }

    @Test
    @DisplayName("печатает только заголовок")
    void printsOnlyHeaderWhenNoQuestions() {
        given(questionDao.findAll()).willReturn(List.of());
        testService.executeTest();
        InOrder io = inOrder(ioService);
        io.verify(ioService).printLine("");
        io.verify(ioService).printFormattedLine("Please answer the questions below%n");
        verify(questionDao).findAll();
        verifyNoMoreInteractions(ioService, questionDao);
    }

    private static Question question(String text, String... answers) {
        return new Question(text, answersOf(answers));
    }

    private static List<Answer> answersOf(String... texts) {
        return java.util.Arrays.stream(texts)
                .map(t -> new Answer(t, false))
                .toList();
    }
}
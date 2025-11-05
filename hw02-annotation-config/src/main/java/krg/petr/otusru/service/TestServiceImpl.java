package krg.petr.otusru.service;

import krg.petr.otusru.dao.QuestionDao;
import krg.petr.otusru.domain.Question;
import krg.petr.otusru.domain.Student;
import krg.petr.otusru.domain.TestResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final IOService ioService;

    private final QuestionDao questionDao;

    @Override
    public TestResult executeTestFor(Student student) {
        ioService.printLine("");
        ioService.printFormattedLine("Please answer the questions below%n");

        var questions = questionDao.findAll();
        var testResult = new TestResult(student);

        for (var question: questions) {
            var isAnswerValid = false;
            ioService.printLine(question.getText());

            for (int i = 0; i < question.getAnswers().size(); i++) {
                ioService.printFormattedLine("%d) %s", i + 1, question.getAnswers().get(i).getText());
            }

            isAnswerValid = getUserAnswerAndValidate(question);
            testResult.applyAnswer(question, isAnswerValid);
        }
        return testResult;
    }

    private boolean getUserAnswerAndValidate(Question question) {
        var answerNumber = ioService.readIntForRangeWithPrompt(1, question.getAnswers().size(),
                "Enter your answer number: ",
                String.format(
                        "Your input is invalid. Please provide a number within the specified range, from 1 to %d!",
                        question.getAnswers().size()));
        return question.answers().get(--answerNumber).isCorrect();
    }
}
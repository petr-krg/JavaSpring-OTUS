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

    private final LocalizedIOService ioService;

    private final QuestionDao questionDao;

    @Override
    public TestResult executeTestFor(Student student) {
        ioService.printLine("");
        ioService.printLineLocalized("TestService.answer.the.questions");
        ioService.printLine("");

        var questions = questionDao.findAll();
        var testResult = new TestResult(student);

        for (var question: questions) {
            var isAnswerValid = false;
            ioService.printLine(question.text());

            for (int i = 0; i < question.answers().size(); i++) {
                ioService.printFormattedLineLocalized("TestService.question.answer.print", i + 1,
                        question.answers().get(i).text());
            }

            isAnswerValid = getUserAnswerAndValidate(question);
            testResult.applyAnswer(question, isAnswerValid);
        }
        return testResult;
    }

    private boolean getUserAnswerAndValidate(Question question) {
        var answerNumber = ioService.readIntForRangeWithPrompt(1, question.answers().size(),
                ioService.getMessage("TestService.input.answer"),
                ioService.getMessage("TestService.answer.is.invalid",
                        1, question.answers().size()));
        return question.answers().get(--answerNumber).isCorrect();
    }

}

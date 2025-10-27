package krg.petr.otusru.service;

import krg.petr.otusru.dao.QuestionDao;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final IOService ioService;

    private final QuestionDao questionDao;

    @Override
    public void executeTest() {
        ioService.printLine("");
        ioService.printFormattedLine("Please answer the questions below%n");
        // Получить вопросы из дао и вывести их с вариантами ответов
        var questions = questionDao.findAll();
        for (var question : questions) {
            ioService.printLine(question.getText());
            for (int i = 0; i < question.getAnswers().size(); i++) {
                ioService.printFormattedLine("%d) %s", i + 1, question.getAnswers().get(i).getText());
            }
        }
    }
}
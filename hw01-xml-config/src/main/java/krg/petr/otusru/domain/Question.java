package krg.petr.otusru.domain;

import java.util.List;

public record Question(String text, List<Answer> answers) {
    public String getText() {
        return text;
    }

    public List<Answer> getAnswers() {
        return answers;
    }
}
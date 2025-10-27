package krg.petr.otusru.domain;

public record Answer(String text, boolean isCorrect) {

    public String getText() {
        return text;
    }

    public boolean isCorrect() {
        return isCorrect;
    }
}
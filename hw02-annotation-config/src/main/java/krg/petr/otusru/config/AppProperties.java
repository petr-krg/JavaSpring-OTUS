package krg.petr.otusru.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppProperties implements TestFileNameProvider, TestConfig {

    private final int rightAnswersCountToPass;

    private final String testFileName;

    public AppProperties(
            @Value("${test.rightAnswersCountToPass}") int rightAnswersCountToPass,
            @Value("${test.fileName}") String testFileName
    ) {
        this.rightAnswersCountToPass = rightAnswersCountToPass;
        this.testFileName = testFileName;
    }

    @Override
    public String getTestFileName() {
        return testFileName;
    }

    @Override
    public int getRightAnswersCountToPass() {
        return rightAnswersCountToPass;
    }
}
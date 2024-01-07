package krg.petr.otusru.config;

import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Locale;
import java.util.Map;


@ConfigurationProperties(prefix = "test")
@NoArgsConstructor
public class AppConfig implements TestConfig, TestFileNameProvider, LocaleConfig {

    private int rightAnswersCountToPass;

    private Locale locale;

    private Map<String, String> fileNameByLocaleTag;

    public void setRightAnswersCountToPass(int rightAnswersCountToPass) {
        this.rightAnswersCountToPass = rightAnswersCountToPass;
    }

    public void setLocale(String locale) {
        this.locale = Locale.forLanguageTag(locale);
    }

    public void setFileNameByLocaleTag(Map<String, String> fileNameByLocaleTag) {
        this.fileNameByLocaleTag = fileNameByLocaleTag;
    }

    @Override
    public int getRightAnswersCountToPass() {
        return rightAnswersCountToPass;
    }

    @Override
    public String getTestFileName() {
        return fileNameByLocaleTag.get(locale.toLanguageTag());
    }

    @Override
    public Locale getLocale() {
        return this.locale;
    }
}
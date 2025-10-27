package krg.petr.otusru;

import krg.petr.otusru.config.AppProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AppProperties")
public class AppPropertiesTest {

    @Test
    @DisplayName("возвращает имя файла из конструктора")
    public void testGetFileName() {
        AppProperties config = new AppProperties("questions.csv");
        assertThat("questions.csv").isEqualTo(config.getTestFileName());
    }
}
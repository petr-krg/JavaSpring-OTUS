package krg.petr.otusru;

import com.opencsv.bean.CsvToBeanBuilder;
import krg.petr.otusru.config.TestFileNameProvider;
import krg.petr.otusru.dao.CsvQuestionDao;
import krg.petr.otusru.dao.dto.QuestionDto;
import krg.petr.otusru.domain.Question;
import krg.petr.otusru.exceptions.QuestionReadException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("CsvQuestionDao")
public class CsvQuestionDaoTest {

    @Mock
    private TestFileNameProvider fileNameProvider;

    @InjectMocks
    private CsvQuestionDao dao;

    @Test
    @DisplayName("findAll() возвращает список вопросов при валидном CSV")
    void findAll_returnsQuestions_whenFileExists() {
        String path = "/questions.csv";
        given(fileNameProvider.getTestFileName()).willReturn(path);
        List<Question> expected = loadQuestionFromFile(path);
        List<Question> actual = dao.findAll();
        assertIterableEquals(expected, actual);
    }

    @Test
    @DisplayName("findAll() бросает QuestionReadException при отсутствии файла")
    void findAll_throws_whenFileMissing() {
        given(fileNameProvider.getTestFileName()).willReturn("/nonexistent-questions.csv");
        assertThrows(QuestionReadException.class, () -> dao.findAll());
    }

    public static List<Question> loadQuestionFromFile(String filePath) {
        List<Question> resultQuestions = new ArrayList<>();
        try (InputStream inputStream = Application.class.getResourceAsStream(filePath)) {
            try (Reader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
                List<QuestionDto> questionDtos = new CsvToBeanBuilder<QuestionDto>(bufferedReader)
                        .withType(QuestionDto.class)
                        .withSkipLines(1)
                        .withSeparator(';')
                        .build()
                        .parse();
                resultQuestions = questionDtos.stream()
                        .map(QuestionDto::toDomainObject)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultQuestions;
    }
}
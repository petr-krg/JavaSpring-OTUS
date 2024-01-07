package krg.petr.otusru.dao;

import com.opencsv.bean.CsvToBeanBuilder;
import krg.petr.otusru.config.TestFileNameProvider;
import krg.petr.otusru.dao.dto.QuestionDto;
import krg.petr.otusru.domain.Question;
import krg.petr.otusru.exceptions.QuestionReadException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class CsvQuestionDao implements QuestionDao {

    private final TestFileNameProvider fileNameProvider;

    @Override
    public List<Question> findAll() {

        System.out.println("TestFileName > " + fileNameProvider.getTestFileName());

        try (InputStream inputStream = getClass().
                getClassLoader().
                getResourceAsStream(fileNameProvider.getTestFileName());
             Reader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {

            List<QuestionDto> questionDtos = new CsvToBeanBuilder<QuestionDto>(bufferedReader)
                    .withType(QuestionDto.class)
                    .withSkipLines(1)
                    .withSeparator(';')
                    .build()
                    .parse();

            return questionDtos.stream()
                    .map(QuestionDto::toDomainObject)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new QuestionReadException("Could not load questions from the resource", e);
        }
    }
}

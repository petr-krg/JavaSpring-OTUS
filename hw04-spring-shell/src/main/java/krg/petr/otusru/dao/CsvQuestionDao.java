package krg.petr.otusru.dao;

import com.opencsv.bean.CsvToBeanBuilder;
import krg.petr.otusru.dao.dto.QuestionDto;
import krg.petr.otusru.exceptions.QuestionReadException;
import lombok.RequiredArgsConstructor;
import krg.petr.otusru.config.TestFileNameProvider;
import krg.petr.otusru.domain.Question;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CsvQuestionDao implements QuestionDao {

    private final TestFileNameProvider fileNameProvider;

    @Override
    public List<Question> findAll() {
        try (InputStream inputStream = getClass().
                getClassLoader().
                getResourceAsStream(fileNameProvider.getTestFileName());
             Reader bufferedReader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream)))) {

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
            var msg = "Could not load questions from the resource: " + fileNameProvider.getTestFileName();
            throw new QuestionReadException(msg, e);
        }
    }
}
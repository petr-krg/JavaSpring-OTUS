package krg.petr.otusru.dao;

import com.opencsv.bean.CsvToBeanBuilder;
import krg.petr.otusru.dao.dto.QuestionDto;
import krg.petr.otusru.exceptions.QuestionReadException;
import lombok.RequiredArgsConstructor;
import krg.petr.otusru.config.TestFileNameProvider;
import krg.petr.otusru.domain.Question;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CsvQuestionDao implements QuestionDao {
    private final TestFileNameProvider fileNameProvider;

    @Override
    public List<Question> findAll() {
        // Использовать CsvToBean
        // https://opencsv.sourceforge.net/#collection_based_bean_fields_one_to_many_mappings
        // Использовать QuestionReadException
        // Про ресурсы: https://mkyong.com/java/java-read-a-file-from-resources-folder/
        try (InputStream inputStream = getClass().getResourceAsStream(fileNameProvider.getTestFileName())) {
            try (Reader bufferedReader = new BufferedReader(
                    new InputStreamReader(Objects.requireNonNull(inputStream)))) {
                List<QuestionDto> questionDtos = new CsvToBeanBuilder<QuestionDto>(bufferedReader)
                        .withType(QuestionDto.class)
                        .withSkipLines(1)
                        .withSeparator(';')
                        .build()
                        .parse();
                return questionDtos.stream()
                        .map(QuestionDto::toDomainObject)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            throw new QuestionReadException("Could not load questions from the resource", e);
        }
    }
}
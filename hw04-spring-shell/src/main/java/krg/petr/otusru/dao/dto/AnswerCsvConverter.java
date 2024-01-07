package krg.petr.otusru.dao.dto;

import com.opencsv.bean.AbstractCsvConverter;
import krg.petr.otusru.domain.Answer;

public class AnswerCsvConverter extends AbstractCsvConverter {

    @Override
    public Object convertToRead(String value) {
        var valueArr = value.split("%");
        return new Answer(valueArr[0], Boolean.parseBoolean(valueArr[1]));
    }
}

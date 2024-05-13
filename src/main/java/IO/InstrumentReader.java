package IO;

import Common.ClientData;
import Common.InstrumentData;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

public class InstrumentReader {
    public static List<InstrumentData> readFrom(boolean test) {
        try {
            // hardcoded for this project.
            FileReader fileReader;
            if (test) {
                fileReader = new FileReader("src/main/resources/test-set/input_instruments.csv");
            } else {
                fileReader = new FileReader("src/main/resources/example-set/input_instruments.csv");
            }
            CSVReader csvReader = new CSVReaderBuilder(fileReader).withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS).withSkipLines(1).build();
            String[] nextRecord;

            List<InstrumentData> list = new ArrayList<>();

            while ((nextRecord = csvReader.readNext()) != null) {
                assert nextRecord.length == 3;

                InstrumentData instrumentData = new InstrumentData(nextRecord[0], nextRecord[1], Integer.parseInt(nextRecord[2]));
                list.add(instrumentData);
            }
            return list;
        } catch (CsvValidationException | IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}

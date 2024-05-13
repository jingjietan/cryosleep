package IO;

import Common.ClientData;
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

public class ClientReader {
    public static List<ClientData> readFrom() {
        try {
            // hardcoded for this project.
            FileReader fileReader = new FileReader("src/main/resources/example-set/input_clients.csv");
            CSVReader csvReader = new CSVReaderBuilder(fileReader).withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS).withSkipLines(1).build();
            String[] nextRecord;

            List<ClientData> list = new ArrayList<>();

            while ((nextRecord = csvReader.readNext()) != null) {
                assert nextRecord.length == 4;
                var positionCheck = true;
                if (nextRecord[2].equals("Y")) {
                } else if (nextRecord[2].equals("N")) {
                    positionCheck = false;
                } else {
                    throw new RuntimeException("Invalid value");
                }
                ClientData clientData = new ClientData(nextRecord[0],
                        new HashSet<>(Arrays.asList(nextRecord[1].split(Pattern.quote(",")))),
                        positionCheck, Integer.parseInt(nextRecord[3]));

                list.add(clientData);
            }
            return list;
        } catch (CsvValidationException | IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}

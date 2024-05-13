import Common.ClientData;
import IO.InstrumentReader;
import IO.OrderReader;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) {
        System.out.println(Paths.get("").toAbsolutePath());

        // track orders rejected
        // track client data
        // track data for each instrument order
        // ----

        // read data from files

        // implement policy checking
        // check at end of auction
        // check at every action in continuous

        // perform open action simulation

        // perform continuous action simulation

        // perform close auction simulation

        // implement
        System.out.println("testing");
    }
}

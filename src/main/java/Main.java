import Common.ClientData;
import Common.InstrumentData;
import IO.ClientReader;
import IO.InstrumentReader;
import IO.OrderReader;
import Matcher.ContinuousMatching;
import Validation.Validation;
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
import java.util.ArrayList;

import Common.ClientData;
import Common.OrderData;
import Repository.MatchOrderRepository;

public class Main {
    public static void main(String[] args) {
        System.out.println(Paths.get("").toAbsolutePath());

        var clientData = ClientReader.readFrom(true);
        var instrumentData = InstrumentReader.readFrom(true);
        var orderData = OrderReader.readFrom(OrderReader.OrderPeriod.Continuous, true);

        var validation = new Validation(clientData, instrumentData, orderData);

        ContinuousMatching matching = new ContinuousMatching(validation, clientData, instrumentData, orderData);
        matching.match();

        validation.writeRejectionTo("src/main/resources/exchange.csv");
        validation.writeClientTo("src/main/resources/client.csv");
        validation.writeInstrumentReport("src/main/resources/report.csv");
    }
}
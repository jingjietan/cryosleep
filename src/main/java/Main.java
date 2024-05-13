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
        // track orders rejected
        // track client data
        // track data for each instrument order
        // ----
        ClientReader cReader = new ClientReader();
        OrderReader oReader = new OrderReader();

        List<OrderData> orders = new ArrayList<OrderData>();
        List<ClientData> clients = new ArrayList<ClientData>();
        // read data from files
        // implement policy checking
        orders = oReader.readFrom(OrderReader.OrderPeriod.Open, false);
        clients = cReader.readFrom(false);
        // implement policy checking functions
        // check at end of auction
        // check at every action in continuous

        // perform open action simulation
        MatchOrderRepository matchOrdersRepository = new MatchOrderRepository(orders, clients);
        System.out.println("Out Auction Output: " + matchOrdersRepository.matchOrders());
        // assumption: Auction has ended

        // perform continuous action simulation
        var clientData = ClientReader.readFrom(true);
        var instrumentData = InstrumentReader.readFrom(true);
        var orderData = OrderReader.readFrom(OrderReader.OrderPeriod.Continuous, true);

        var validation = new Validation(clientData, instrumentData, orderData);

        ContinuousMatching matching = new ContinuousMatching(validation, clientData, instrumentData, orderData);
        matching.match();

        // perform close auction simulation

        // implement
        System.out.println("testing");
    }
}
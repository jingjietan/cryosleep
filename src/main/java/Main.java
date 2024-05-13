import Common.ClientData;
import Common.InstrumentData;
import IO.ClientReader;
import IO.InstrumentReader;
import IO.OrderReader;
import Matcher.ContinuousMatching;
import Repository.TradeResult;
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
        orders = oReader.readFrom(OrderReader.OrderPeriod.Open, true);
        clients = cReader.readFrom(true);
        var instrumentData = InstrumentReader.readFrom(true);
        // implement policy checking functions
        // check at end of auction
        // check at every action in continuous
        var validation = new Validation(clients, instrumentData, orders);

        // perform open action simulation
        MatchOrderRepository matchOrdersRepository = new MatchOrderRepository(orders, clients, validation);
        // assumption: Auction has ended
        TradeResult tradeResult = matchOrdersRepository.matchOrders();
        System.out.println("===== AUCTION =====");
        System.out.println("Out Auction Output: " + tradeResult.getMaxTradeQuantity());
        List<OrderData> combinedList = new ArrayList<>();
        combinedList.addAll(tradeResult.getBuyOrders());
        combinedList.addAll(tradeResult.getSellOrders());

        System.out.println("===== LIVE =====");
        System.out.println(combinedList);
        // perform continuous action simulation
        ContinuousMatching matching = new ContinuousMatching(validation, clients, instrumentData, combinedList);
        matching.match();
        System.out.println("===== CLOSE =====");

        List<OrderData> combinedListForClose = new ArrayList<>();
        combinedListForClose.addAll(matching.getBuyBook());
        combinedListForClose.addAll(matching.getSellBook());
        System.out.println(combinedListForClose);
        // perform close auction simulation
        MatchOrderRepository closeOrdersRepository = new MatchOrderRepository(combinedListForClose, clients, validation);
        System.out.println(closeOrdersRepository.matchOrders().getMaxTradeQuantity());
    }
}
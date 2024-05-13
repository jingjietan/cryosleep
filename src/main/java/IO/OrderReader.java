package IO;

import Common.BuySell;
import Common.OrderData;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.function.Function;

public class OrderReader {
    public enum OrderPeriod {
        Open,
        Continuous,
        Close,
        All
    }

    private static Time openAuctionStart = Time.valueOf("09:00:00");
    private static Time openAuctionEnd = Time.valueOf("09:30:00");
    private static Time continuousAuctionStart = Time.valueOf("09:30:00");
    private static Time continuousAuctionEnd = Time.valueOf("16:00:00");
    private static Time closeAuctionStart = Time.valueOf("16:00:00");
    private static Time closeAuctionEnd = Time.valueOf("16:30:00");


    public static List<OrderData> readFrom(OrderPeriod period, boolean test) {
        try {
            // hardcoded for this project.
            FileReader fileReader;
            if (test) {
                fileReader = new FileReader("src/main/resources/test-set/input_orders.csv");
            } else {
                fileReader = new FileReader("src/main/resources/example-set/input_orders.csv");
            }
            CSVReader csvReader = new CSVReaderBuilder(fileReader).withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS).withSkipLines(1).build();
            String[] nextRecord;

            Function<Time, Boolean> canMatch;
            switch (period) {
                case All -> canMatch = time -> true;
                case Open -> canMatch = time -> time.after(openAuctionStart) && time.before(openAuctionEnd);
                case Continuous -> canMatch = time -> time.after(continuousAuctionStart) && time.before(continuousAuctionEnd);
                case Close -> canMatch = time -> time.after(closeAuctionStart) && time.before(closeAuctionEnd);
                default -> throw new RuntimeException("");
            }

            List<OrderData> list = new ArrayList<>();

            while ((nextRecord = csvReader.readNext()) != null) {
                assert nextRecord.length == 7;

                Time time = Time.valueOf(nextRecord[0]);
                if (!canMatch.apply(time)) {
                    continue;
                }

                String order = nextRecord[1];
                String instrument = nextRecord[2];
                String client = nextRecord[4];
                BigDecimal price;
                if (nextRecord[5].equals("Market")) {
                    price = BigDecimal.ZERO;
                } else {
                    price = BigDecimal.valueOf(Double.parseDouble(nextRecord[5]));
                }

                var quantity = Double.valueOf(nextRecord[3]).intValue();

                BuySell side;
                if (nextRecord[6].equals("Sell")) {
                    side = BuySell.Sell;
                } else if (nextRecord[6].equals("Buy")) {
                    side = BuySell.Buy;
                } else {
                    throw new RuntimeException("Invalid side " + nextRecord[6]);
                }

                OrderData orderData = new OrderData(time, order, client, instrument, side, price, quantity);
                list.add(orderData);
            }
            return list;
        } catch (CsvValidationException | IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    public static List<OrderData> readFrom(OrderPeriod period, String filePath) {
        try {
            // hardcoded for this project.
            FileReader fileReader = new FileReader(filePath);
            CSVReader csvReader = new CSVReaderBuilder(fileReader).withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS).withSkipLines(1).build();
            String[] nextRecord;

            Function<Time, Boolean> canMatch;
            switch (period) {
                case All -> canMatch = time -> true;
                case Open -> canMatch = time -> time.after(openAuctionStart) && time.before(openAuctionEnd);
                case Continuous -> canMatch = time -> time.after(continuousAuctionStart) && time.before(continuousAuctionEnd);
                case Close -> canMatch = time -> time.after(closeAuctionStart) && time.before(closeAuctionEnd);
                default -> throw new RuntimeException("");
            }

            List<OrderData> list = new ArrayList<>();

            while ((nextRecord = csvReader.readNext()) != null) {
                assert nextRecord.length == 7;

                Time time = Time.valueOf(nextRecord[0]);
                if (!canMatch.apply(time)) {
                    continue;
                }

                String order = nextRecord[1];
                String instrument = nextRecord[2];
                String client = nextRecord[4];
                BigDecimal price;
                if (nextRecord[5].equals("Market")) {
                    price = BigDecimal.ZERO;
                } else {
                    price = BigDecimal.valueOf(Double.parseDouble(nextRecord[5]));
                }

                var quantity = Double.valueOf(nextRecord[3]).intValue();

                BuySell side;
                if (nextRecord[6].equals("Sell")) {
                    side = BuySell.Sell;
                } else if (nextRecord[6].equals("Buy")) {
                    side = BuySell.Buy;
                } else {
                    throw new RuntimeException("Invalid side " + nextRecord[6]);
                }

                OrderData orderData = new OrderData(time, order, client, instrument, side, price, quantity);
                list.add(orderData);
            }
            return list;
        } catch (CsvValidationException | IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}

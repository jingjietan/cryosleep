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

    private static Date openAuctionStart = new GregorianCalendar(0, 0, 1, 9, 0).getTime();
    private static Date openAuctionEnd = new GregorianCalendar(0, 0, 1, 9, 30).getTime();
    private static Date continuousAuctionStart = new GregorianCalendar(0, 0, 1, 9, 30).getTime();
    private static Date continuousAuctionEnd = new GregorianCalendar(0, 0, 1, 16, 0).getTime();
    private static Date closeAuctionStart = new GregorianCalendar(0, 0, 1, 16, 0).getTime();
    private static Date closeAuctionEnd = new GregorianCalendar(0, 0, 1, 16, 30).getTime();


    public static List<OrderData> readFrom(OrderPeriod period) {
        try {
            // hardcoded for this project.
            FileReader fileReader = new FileReader("src/main/resources/example-set/input_orders.csv");
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

                var quantity = Integer.parseInt(nextRecord[3]);

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

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
import java.util.ArrayList;
import java.util.List;

public class OrderReader {
    public static List<OrderData> readFrom() {
        try {
            // hardcoded for this project.
            FileReader fileReader = new FileReader("src/main/resources/example-set/input_orders.csv");
            CSVReader csvReader = new CSVReaderBuilder(fileReader).withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS).withSkipLines(1).build();
            String[] nextRecord;

            List<OrderData> list = new ArrayList<>();

            while ((nextRecord = csvReader.readNext()) != null) {
                assert nextRecord.length == 7;

                String time = nextRecord[0];
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

                OrderData orderData = new OrderData(nextRecord[0], order, client, instrument, side, price, quantity);
                list.add(orderData);
            }
            return list;
        } catch (CsvValidationException | IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}

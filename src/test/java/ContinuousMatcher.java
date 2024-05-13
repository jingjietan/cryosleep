import Common.BuySell;
import Common.OrderData;
import IO.ClientReader;
import IO.InstrumentReader;
import IO.OrderReader;
import Matcher.ContinuousMatching;
import Validation.Validation;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Time;

public class ContinuousMatcher {
    @Test
    public void testContinuousMatching() {
        var clientData = ClientReader.readFrom(false);
        var instrumentData = InstrumentReader.readFrom(false);
        var orderData = OrderReader.readFrom(OrderReader.OrderPeriod.Continuous, false);

        var verifier = new Validation(clientData, instrumentData, orderData);
        var matcher = new ContinuousMatching(verifier, clientData, instrumentData, orderData);
        matcher.addSellOrder(new OrderData(Time.valueOf("09:00:01"), "B1", "B", "SIA", BuySell.Sell, BigDecimal.valueOf(32.1), 4000));
        matcher.addBuyOrder(new OrderData(Time.valueOf("09:05:00"), "C1", "C", "SIA", BuySell.Buy, BigDecimal.valueOf(32.0), 100));
        matcher.addBuyOrder(new OrderData(Time.valueOf("09:29:03"), "A2", "A", "SIA", BuySell.Buy, BigDecimal.valueOf(31.9), 800));
        matcher.match();

        System.out.println(matcher.getBuyBook());
        System.out.println(matcher.getSellBook());
    }
}


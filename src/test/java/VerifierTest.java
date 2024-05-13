import Common.BuySell;
import Common.OrderData;
import IO.ClientReader;
import IO.InstrumentReader;
import IO.OrderReader;
import Validation.Validation;
import org.junit.Test;
import org.junit.Assert.*;

import java.math.BigDecimal;
import java.sql.Time;

import static junit.framework.TestCase.assertEquals;

public class VerifierTest {
    @Test
    public void testVerifier() {
        var clientData = ClientReader.readFrom(false);
        var instrumentData = InstrumentReader.readFrom(false);
        var orderData = OrderReader.readFrom(OrderReader.OrderPeriod.Continuous, false);

        var correct = new OrderData(Time.valueOf("09:05:00"), "C1", "C", "SIA", BuySell.Buy, BigDecimal.valueOf(32.0), 100);
        var error1 = new OrderData(Time.valueOf("09:10:00"), "D1", "D", "SIA", BuySell.Sell, BigDecimal.ZERO, 300);
        var error2 = new OrderData(Time.valueOf("09:29:01"), "B2", "B", "SIA", BuySell.Sell, BigDecimal.valueOf(32.1), 5);
        var verifier = new Validation(clientData, instrumentData, orderData);
        assertEquals(verifier.verify(correct), true);
        assertEquals(verifier.verify(error1), false);
        assertEquals(verifier.verify(error2), false);
    }
}

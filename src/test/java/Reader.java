import Common.BuySell;
import IO.InstrumentReader;
import IO.OrderReader;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class Reader {
    @Test
    public void testClientReader() {
        var data = IO.ClientReader.readFrom(false);
        assertEquals(data.get(0).positionCheck, true);
        assertEquals(data.get(1).positionCheck, false);
    }

    @Test
    public void testOrderReader() {
        var data = OrderReader.readFrom(OrderReader.OrderPeriod.All, false);
        assertEquals(data.get(0).quantity, 1500);
        assertEquals(data.get(1).price, BigDecimal.valueOf(32.1));
        assertEquals(data.get(2).side, BuySell.Buy);
    }

    @Test
    public void testInstrumentReader() {
        var data = InstrumentReader.readFrom(false);
        assertEquals(data.get(0).currency, "SGD");
        assertEquals(data.get(0).lotSize, 100);
    }
}

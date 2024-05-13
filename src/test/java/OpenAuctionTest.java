import Common.ClientData;
import Common.OrderData;
import IO.ClientReader;
import IO.OrderReader;
import Repository.MatchOrderRepository;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OpenAuctionTest {
    @Test
    public void testSomeMethod() {
        ClientReader cReader = new ClientReader();
        OrderReader oReader = new OrderReader();

        List<OrderData> orders = new ArrayList<OrderData>();
        List<ClientData> clients = new ArrayList<ClientData>();
        // read data from files
        // implement policy checking
        orders = oReader.readFrom(OrderReader.OrderPeriod.Open, "src/main/resources/example-set/input_ordersTest.csv");
        clients = cReader.readFrom(false);
        // implement policy checking functions
        // check at end of auction
        // check at every action in continuous

        // perform open action simulation
        MatchOrderRepository matchOrdersRepository = new MatchOrderRepository(orders, clients);
        double value = 32.1;
        BigDecimal expectedValue = new BigDecimal("32.1");
        BigDecimal actualValue = matchOrdersRepository.matchOrders();

        assertEquals(0, actualValue.compareTo(expectedValue));
    }
}
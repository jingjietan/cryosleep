package Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import Common.OrderData;

public class TradeResult {
    private BigDecimal maxTradeQuantity;
    private List<OrderData> buyOrders;
    private List<OrderData> sellOrders;

    public TradeResult(BigDecimal maxTradeQuantity, List<OrderData> buyOrders, List<OrderData> sellOrders) {
        this.maxTradeQuantity = maxTradeQuantity;
        this.buyOrders = buyOrders != null ? buyOrders : new ArrayList<>();
        this.sellOrders = sellOrders != null ? sellOrders : new ArrayList<>();
    }

    public BigDecimal getMaxTradeQuantity() {
        return maxTradeQuantity;
    }

    public List<OrderData> getBuyOrders() {
        return buyOrders;
    }

    public List<OrderData> getSellOrders() {
        return sellOrders;
    }
}


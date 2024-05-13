package Common;

import java.math.BigDecimal;

public class OrderData {
    public String time;
    public String orderID;
    public String client;
    public String instrument;
    public BuySell side;
    public BigDecimal price; // 0 is MARKET
    public int quantity;

    public OrderData(String time, String orderID, String client, String instrument, BuySell side, BigDecimal price, int quantity) {
        this.time = time;
        this.orderID = orderID;
        this.client = client;
        this.instrument = instrument;
        this.side = side;
        this.price = price;
        this.quantity = quantity;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getInstrument() {
        return instrument;
    }

    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }

    public BuySell getSide() {
        return side;
    }

    public void setSide(BuySell side) {
        this.side = side;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "OrderData{" +
                "time='" + time + '\'' +
                ", orderID='" + orderID + '\'' +
                ", client='" + client + '\'' +
                ", instrument='" + instrument + '\'' +
                ", side=" + side +
                ", price=" + price +
                ", quantity=" + quantity +
                '}';
    }
}

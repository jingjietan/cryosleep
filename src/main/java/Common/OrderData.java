package Common;

import java.math.BigDecimal;
import java.sql.Time;

public class OrderData implements Cloneable {
    public Time time;
    public String orderID;
    public String client;
    public String instrument;
    public BuySell side;
    public BigDecimal price; // 0 is MARKET
    public int quantity;

    public OrderData(Time time, String orderID, String client, String instrument, BuySell side, BigDecimal price, int quantity) {
        this.time = time;
        this.orderID = orderID;
        this.client = client;
        this.instrument = instrument;
        this.side = side;
        this.price = price;
        this.quantity = quantity;
    }

    public Time getTime() {
        return time;
    }

    public void setTime(Time time) {
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

    public boolean isMarket() {
        return price.equals(BigDecimal.ZERO);
    }

    public boolean isDepleted() {
        return this.quantity == 0;
    }

    // Deduct both sides till one order data is depleted.
    public int deduct(OrderData orderData) {
        var deductable = Math.min(this.quantity, orderData.quantity);
        this.quantity -= deductable;
        orderData.quantity -= deductable;
        return deductable;
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

    @Override
    public OrderData clone() {
        try {
            return (OrderData) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}

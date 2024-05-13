package Matcher;

import Common.ClientData;
import Common.InstrumentData;
import Common.OrderData;
import Validation.Validator;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;

public class ContinuousMatching {
    private Validator validator;
    private Map<String, ClientData> clients;
    private List<InstrumentData> instruments;
    private List<OrderData> orders;
    private PriorityQueue<OrderData> buyBook;
    private PriorityQueue<OrderData> sellBook;
    private Comparator<OrderData> getBookOrderComparator(boolean maximum) {
        return (o1, o2) -> {
            int comp = o1.price.compareTo(o2.price);
            if (comp != 0) {
                if (maximum) {
                    return comp;
                } else {
                    return -comp;
                }
            }

            Integer rating1 = clients.get(o1.client).rating;
            Integer rating2 = clients.get(o2.client).rating;

            int comp2 = rating1.compareTo(rating2);
            if (comp2 != 0) {
                return comp2;
            }

            return o1.time.compareTo(o2.time);
        };
    }

    public ContinuousMatching(Validator validator, List<ClientData> clients, List<InstrumentData> instruments, List<OrderData> orders) {
        this.validator = validator;

        this.clients = new HashMap<>();
        for (var client: clients) {
            this.clients.put(client.clientID, client);
        }
        this.instruments = instruments;
        this.orders = orders;

        buyBook = new PriorityQueue<>(getBookOrderComparator(false)); // buy low
        sellBook = new PriorityQueue<>(getBookOrderComparator(true)); // sell high
    }

    public void match() {
        // assuming orders are according to time

        for (var order: orders) {
            if (!validator.verify(order)) {
                continue;
            }

            switch (order.side) {
                case Buy -> buyBook.add(order);
                case Sell -> sellBook.add(order);
            }

            resolve();
        }
    }

    private void resolve() {
        OrderData buyData;
        OrderData sellData;

        while (true) {
            buyData = buyBook.peek();
            sellData = sellBook.peek();
            if (buyData == null || sellData == null) {
                // No more buy/sell to match
                return;
            }
            if (sellData.price.compareTo(buyData.price) <= 0) { // Sell <= Buy
                int deducted = sellData.deduct(buyData);
                validator.recordTranscation(sellData.instrument, buyData.client, sellData.client, deducted);

                if (sellData.isDepleted()) {
                    sellBook.remove();
                }
                if (buyData.isDepleted()) {
                    buyBook.remove();
                }
            } else {
                // No possible match
                return;
            }
        }
    }

    /// For testing
    public void addBuyOrder(OrderData orderData) {
        this.buyBook.add(orderData);
    }

    /// For testing
    public void addSellOrder(OrderData orderData) {
        this.sellBook.add(orderData);
    }

    public PriorityQueue<OrderData> getBuyBook() {
        return buyBook;
    }

    public PriorityQueue<OrderData> getSellBook() {
        return sellBook;
    }
}

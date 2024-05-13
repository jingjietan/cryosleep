package Matcher;

import Common.BuySell;
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
    private Map<String, PriorityQueue<OrderData>> buyBook;
    private Map<String, PriorityQueue<OrderData>> sellBook;
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

    private OrderData latestOrder = null;

    public ContinuousMatching(Validator validator, List<ClientData> clients, List<InstrumentData> instruments, List<OrderData> orders) {
        this.validator = validator;

        this.clients = new HashMap<>();
        for (var client: clients) {
            this.clients.put(client.clientID, client);
        }
        this.instruments = instruments;
        this.orders = orders;

        buyBook = new HashMap<>();
                //new PriorityQueue<>(getBookOrderComparator(false)); // buy low
        sellBook = new HashMap<>();
                //= new PriorityQueue<>(getBookOrderComparator(true)); // sell high
    }

    public void match() {
        // assuming orders are according to time

        for (var order: orders) {
            if (!validator.verify(order)) {
                continue;
            }



            switch (order.side) {
                case Buy -> {
                    getBuyBook(order.instrument).add(order);
                }
                case Sell -> {
                    getSellBook(order.instrument).add(order);
                }
            }
            latestOrder = order;

            resolve(order.instrument);
        }
    }

    private void resolve(String instrument) {
        OrderData buyData;
        OrderData sellData;

        while (true) {
            buyData = getBuyBook(instrument).peek();
            sellData = getSellBook(instrument).peek();
            if (buyData == null || sellData == null) {
                // No more buy/sell to match
                return;
            }

            if (sellData.price.compareTo(buyData.price) <= 0) { // Sell <= Buy
                int deducted = sellData.deduct(buyData);
                validator.recordTranscation(sellData.instrument, buyData.client, sellData.client, deducted, buyData.price);

                if (sellData.isDepleted()) {
                    getSellBook(instrument).remove();
                }
                if (buyData.isDepleted()) {
                    getBuyBook(instrument).remove();
                }
            } else {
                // No possible match
                return;
            }
        }
    }

    /// For testing
    public void addBuyOrder(OrderData orderData) {
        getBuyBook(orderData.instrument).add(orderData);
    }

    /// For testing
    public void addSellOrder(OrderData orderData) {
        getSellBook(orderData.instrument).add(orderData);
    }

    public Map<String, PriorityQueue<OrderData>> getBuyBook() {
        return buyBook;
    }

    public Map<String, PriorityQueue<OrderData>> getSellBook() {
        return sellBook;
    }

    public PriorityQueue<OrderData> getBuyBook(String asset) {
        if (!buyBook.containsKey(asset)) {
            buyBook.put(asset, new PriorityQueue<>(getBookOrderComparator(false)));
        }
        return buyBook.get(asset);
    }

    public PriorityQueue<OrderData> getSellBook(String asset) {
        if (!sellBook.containsKey(asset)) {
            sellBook.put(asset, new PriorityQueue<>(getBookOrderComparator(true)));
        }
        return sellBook.get(asset);
    }
}

package Matcher;

import Common.ClientData;
import Common.InstrumentData;
import Common.OrderData;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;

public class ContinuousMatching {
    public Consumer<Integer> registerCallback;

    private Map<String, ClientData> clients;
    private List<InstrumentData> instruments;
    private List<OrderData> orders;

    private PriorityQueue<OrderData> buyBook;

    /**
     * Get the largest buy order price possible. Used to match sell orders.
     * Does not modify the book.
     */
    private Optional<OrderData> getLargestBuyOrderPossible(BigDecimal price, boolean market) {
        var data = buyBook.peek();
        if (data == null) {
            return Optional.empty();
        }
        if (!market && data.price.compareTo(price) >= 0) {
            return Optional.empty();
        }

        return Optional.of(data);
    }

    /**
     * Get the smallest sell order price possible. Used to match buy orders.
     * Does not modify the book.
     */
    private Optional<OrderData> getSmallestSellOrderPossible(BigDecimal price, boolean market) {
        var data = sellBook.peek();
        if (data == null) {
            return Optional.empty();
        }
        if (!market && data.price.compareTo(price) <= 0) {
            return Optional.empty();
        }

        return Optional.of(data);
    }

    private PriorityQueue<OrderData> sellBook;

    private Comparator<OrderData> getBookOrderComparator() {
        return (o1, o2) -> {
            int comp = o1.price.compareTo(o2.price);
            if (comp != 0) {
                return comp;
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

    public ContinuousMatching(Consumer<Integer> registerCallback, List<ClientData> clients, List<InstrumentData> instruments, List<OrderData> orders) {
        this.registerCallback = registerCallback;

        this.clients = new HashMap<>();
        for (var client: clients) {
            this.clients.put(client.clientID, client);
        }
        this.instruments = instruments;
        this.orders = orders;

        buyBook = new PriorityQueue<>(getBookOrderComparator());
        sellBook = new PriorityQueue<>(getBookOrderComparator());
    }

    public void match() {
        // assuming orders are according to time

        for (var order: orders) {
            // if order is valid

            switch (order.side) {
                case Buy -> buy(order);
                case Sell -> sell(order);
            }
        }
    }

    private void buy(OrderData orderData) {
        Optional<OrderData> data;
        while ((data = getLargestBuyOrderPossible(orderData.price, orderData.isMarket())).isPresent()) {
            var match = data.get();
            orderData.deduct(match);

            if (match.isDepleted()) {
                buyBook.remove(match);
            }
            if (orderData.isDepleted()) {
                return;
            }
        }
    }

    private void sell(OrderData orderData) {
        Optional<OrderData> data;
        while ((data = getSmallestSellOrderPossible(orderData.price, orderData.isMarket())).isPresent()) {
            var match = data.get();
            orderData.deduct(match);

            if (match.isDepleted()) {
                sellBook.remove(match);
            }
            if (orderData.isDepleted()) {
                return;
            }
        }
    }
}

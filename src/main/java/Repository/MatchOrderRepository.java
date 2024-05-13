package Repository;

import java.math.BigDecimal;
import java.util.*;

import Common.BuySell;
import Common.ClientData;
import Common.OrderData;

public class MatchOrderRepository {
    private List<OrderData> orderBook;
    private List<ClientData> clientData;
    private Map<String, Integer> clientRatings;
    private PriorityQueue<OrderData> priorityQueue;
    private BigDecimal openPrice;

    public MatchOrderRepository(List<OrderData> orders, List<ClientData> clients) {
        // Initialize orderBook with Orders and Client data
        orderBook = new ArrayList<OrderData>();
        clientData = new ArrayList<ClientData>();
        priorityQueue = new PriorityQueue<>(Comparator.comparingInt(order -> clientRatings.get(order.client)));
        clientRatings = new HashMap<>();
        openPrice = BigDecimal.ZERO;

        orderBook = orders;
        clientData = clients;
        for (ClientData client : clientData) {
            clientRatings.put(client.clientID, client.rating);
        }
        priorityQueue = new PriorityQueue<>(Comparator.comparingInt(order -> clientRatings.get(order.client)));
    }

    // SELL: MARKET > lower price > smaller (higher) rating > smaller (earlier) arrival time
    // BUY: higher price > smaller (higher) rating > smaller (earlier) arrival time > MARKET
    public BigDecimal matchOrders() {
        // -Set the open price first (if there are buy/sell orders with MARKET price)-
        // INITIALISE: Gather all possible match prices
        Set<BigDecimal> uniqueBuyPrices = new HashSet<BigDecimal>();
        Set<BigDecimal> uniqueSellPrices = new HashSet<BigDecimal>();
        int marketBuyOrders = 0;
        int marketSellOrders = 0;
        for (OrderData order : orderBook) {
            if (order.side == BuySell.Buy) {
                // Check if market price exists
                if (order.price == BigDecimal.ZERO) {
                    marketBuyOrders++;
                }
                uniqueBuyPrices.add(order.price);
            } else if (order.side == BuySell.Sell) {
                if (order.price == BigDecimal.ZERO) {
                    marketSellOrders++;
                }
                uniqueSellPrices.add(order.price);
            }
        }

        // EDGE CASE: all buy/sell orders are market orders
        if (marketBuyOrders + marketSellOrders == orderBook.size()) {
            // Return 0 as open price
            return BigDecimal.ZERO;
        }

        // If there are market orders in BUY side, we can explore more possible match prices in the SELL side
        if (marketBuyOrders > 0) {
            // Find the maximum value in uniqueBuyPrices
            BigDecimal maxBuyPrice = uniqueBuyPrices.stream()
                .reduce(BigDecimal::max)  // Find the maximum BigDecimal
                .orElse(BigDecimal.ZERO); // Provide a default value if the set is empty
            // Iterate over uniqueSellPrices and add prices higher than maxBuyPrice to uniqueBuyPrices
            for (BigDecimal sellPrice : uniqueSellPrices) {
                if (sellPrice.compareTo(maxBuyPrice) > 0) {
                    uniqueBuyPrices.add(sellPrice);
                }
            }
        }

        // Initialize an integer array with the same size as uniqueBuyPrices
        int[] quantityPerPrice = new int[uniqueBuyPrices.size()];
        for (int i = 0; i < quantityPerPrice.length; i++) {
            quantityPerPrice[i] = 0;
        }

        // CASE: Check the quantities of all possible open prices
        for (BigDecimal possibleOpenPrice : uniqueBuyPrices) {
            // Populate all priority queues
            // BUY SIDE pq
            PriorityQueue<OrderData> buyQueue = new PriorityQueue<>(
                Comparator.comparingDouble(
                        (OrderData order) -> order.getPrice().doubleValue())
                    .thenComparingInt(order -> clientRatings.get(order.getClient()))
                    .thenComparingLong(order -> order.getTime().toLocalTime().toSecondOfDay())
            );
            // SELL SIDE pq
            PriorityQueue<OrderData> sellQueue = new PriorityQueue<>(
                Comparator.comparingDouble(
                        (OrderData order) -> order.getPrice().doubleValue())
                    .thenComparingInt(order -> clientRatings.get(order.getClient()))
                    .thenComparingLong(order -> order.getTime().toLocalTime().toSecondOfDay())
            );

            for (OrderData order : orderBook) {
                // Populate all buys >= open price or MARKET
                if (order.getSide() == BuySell.Buy) {
                    // Add buy orders
                    if (order.getPrice().compareTo(possibleOpenPrice) >= 0 || order.getPrice().equals(BigDecimal.ZERO)) {
                        buyQueue.add(order);
                    }
                }
                // Populate all sells <= open price or MARKET
                else {
                    if (order.getPrice().compareTo(possibleOpenPrice) <= 0 || order.getPrice().equals(BigDecimal.ZERO)) {
                        sellQueue.add(order);
                    }
                }
            }
            System.out.println(buyQueue);
            // Simulate buys one by one
            while (buyQueue.size() > 0) {
                int tradeQuantity = 0;
                // Current buy order
                OrderData buyOrder = buyQueue.poll();
                System.out.println(buyOrder);
                // If this order is market,
                if (buyOrder.getPrice().compareTo(BigDecimal.ZERO) == 0) {
                    buyOrder.setPrice(possibleOpenPrice);
                }
                // Make a deep copy of the sell queue
                PriorityQueue<OrderData> sellQueueCopy = new PriorityQueue<>(sellQueue.size(), sellQueue.comparator());
                for (OrderData order : sellQueue) {
                    sellQueueCopy.add(order.clone());
                }

                // Simulate purchases
                while (sellQueueCopy.size() > 0) {
                    // If sell order > buy order, add sell order back to pq and continue to next buy order
                    // If buy order > sell order, update buy order with reduced quantity
                    OrderData sellOrder = sellQueueCopy.poll();
                    if (sellOrder.getQuantity() > buyOrder.getQuantity()) {
                        int quantityChange = sellOrder.getQuantity() - buyOrder.getQuantity();
                        sellOrder.setQuantity(quantityChange);
                        sellQueueCopy.add(sellOrder);
                        tradeQuantity += quantityChange;
                        break;
                    } else if (buyOrder.getQuantity() > sellOrder.getQuantity()) {
                        int quantityChange = buyOrder.getQuantity() - sellOrder.getQuantity();
                        buyOrder.setQuantity(quantityChange);
                        tradeQuantity += quantityChange;
                    }
                }
            }
        }

        // FINAL: if no failures so far
        int maxValue = 0;
        int maxIndex = -1;
        for (int i = 0; i < quantityPerPrice.length; i++) {
            if (quantityPerPrice[i] > maxValue) {
                maxValue = quantityPerPrice[i];
                maxIndex = i;
            }
        }
        // CASE: no buy price overlaps with sell price
        // Return NULL for OpenPrice - indication to set open price as the first trade happened in continuous session
        if (maxIndex == -1) {
            // Return 0 as open price
            return BigDecimal.ZERO;
        }
        List<BigDecimal> uniqueBuyPricesList = new ArrayList<>(uniqueBuyPrices);
        return uniqueBuyPricesList.get(maxIndex);
    };
};
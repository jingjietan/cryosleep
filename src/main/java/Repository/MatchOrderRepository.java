package Repository;

import java.math.BigDecimal;
import java.util.*;

import Common.BuySell;
import Common.ClientData;
import Common.OrderData;
import Validation.Validator;

public class MatchOrderRepository {
    private List<OrderData> orderBook;
    private List<ClientData> clientData;
    private Map<String, Integer> clientRatings;
    private PriorityQueue<OrderData> priorityQueue;
    private BigDecimal openPrice;
    private Validator validator;

    public MatchOrderRepository(List<OrderData> orders, List<ClientData> clients, Validator validator) {
        // Initialize orderBook with Orders and Client data
        orderBook = new ArrayList<OrderData>();
        clientData = new ArrayList<ClientData>();
        priorityQueue = new PriorityQueue<>(Comparator.comparingInt(order -> clientRatings.get(order.client)));
        clientRatings = new HashMap<>();
        this.validator = validator;
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
    public TradeResult matchOrders() {
        // -Set the open price first (if there are buy/sell orders with MARKET price)-
        // INITIALISE: Gather all possible match prices
        Set<BigDecimal> uniqueBuyPrices = new HashSet<BigDecimal>();
        Set<BigDecimal> uniqueSellPrices = new HashSet<BigDecimal>();
        int marketBuyOrders = 0;
        int marketSellOrders = 0;
        for (OrderData order : orderBook) {
            System.out.println(validator.verify(order));
            if (!validator.verify(order)) {
                continue;
            }
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
            return new TradeResult(BigDecimal.ZERO, null, null);
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
        int quantityIndex = 0;

        // Sort the buy prices from small to big
        List<BigDecimal> sortedPrices = new ArrayList<>(uniqueBuyPrices);
        Collections.sort(sortedPrices);

        Set<BigDecimal> sortedUniqueBuyPrices = new LinkedHashSet<>(sortedPrices);

        System.out.print("All open prices: ");
        for (BigDecimal price : sortedUniqueBuyPrices) {
            System.out.print(price + " ");
        }
        System.out.println("");
        System.out.println("=======");
        int highestQty = 0;
        PriorityQueue<OrderData> buyQueueBest = null;
        PriorityQueue<OrderData> sellQueueBest = null;
        // CASE: Check the quantities of all possible open prices
        for (BigDecimal possibleOpenPrice : sortedUniqueBuyPrices) {
            if (possibleOpenPrice == BigDecimal.ZERO) {
                continue;
            }
            System.out.println("Current open price considered: " + possibleOpenPrice);
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

            // Populate all priority queues
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
            // This works - System.out.println("At this open price, here are the buy orders: " + buyQueue);
            // Make a deep copy of the buy queue
            PriorityQueue<OrderData> buyQueueCopy = new PriorityQueue<>(buyQueue.size(), buyQueue.comparator());
            for (OrderData order : buyQueue) {
                buyQueueCopy.add(order.clone());
            }

            PriorityQueue<OrderData> sellQueueCopy = null; // Initialize sellQueueCopy to null
            if (sellQueue.size() > 0) {
                // Make a deep copy of the sell queue
                sellQueueCopy = new PriorityQueue<>(sellQueue.size(), sellQueue.comparator());
                for (OrderData order : sellQueue) {
                    sellQueueCopy.add(order.clone());
                }
            }
            int tradeQuantity = 0;
            // Simulate buys one by one if there are matching sells
            if (sellQueue.size() > 0){
                while (buyQueueCopy.size() > 0 && sellQueue.size() > 0) {
                    // Current buy order
                    OrderData buyOrder = buyQueueCopy.poll();
                    // If this order is market,
                    if (buyOrder.getPrice().compareTo(BigDecimal.ZERO) == 0) {
                        System.out.printf("(Market Order) ");
                        buyOrder.setPrice(possibleOpenPrice);
                    }
                    System.out.println("Current buy order: " + buyOrder);
                    // Simulate purchases
                    while (sellQueueCopy.size() > 0) {
                        // If sell order > buy order, add sell order back to pq and continue to next buy order
                        // If buy order > sell order, update buy order with reduced quantity
                        // If same quantity, continue to next buy order
                        OrderData sellOrder = sellQueueCopy.poll();

//                        System.out.println("In this buy order, buying from: " + sellOrder);
                        if (sellOrder.getQuantity() > buyOrder.getQuantity()) {
                            sellOrder.setQuantity(sellOrder.getQuantity() - buyOrder.getQuantity());
                            sellQueueCopy.add(sellOrder);
                            tradeQuantity += buyOrder.getQuantity();
                            System.out.println("Buyer " + buyOrder.client + " - Trade " + buyOrder.getOrderID() + " "  + buyOrder.getQuantity() + "@" + possibleOpenPrice + " - Seller " + sellOrder.client);
                            break;
                        } else if (buyOrder.getQuantity() > sellOrder.getQuantity()) {
                            int quantityChange = buyOrder.getQuantity() - sellOrder.getQuantity();
                            buyOrder.setQuantity(quantityChange);
                            System.out.println("Buyer " + buyOrder.client + " - Trade " + buyOrder.getOrderID() + " " + sellOrder.getQuantity() + "@" + possibleOpenPrice + " - Seller " + sellOrder.client);
                            tradeQuantity += sellOrder.getQuantity();
                        } else {
                            tradeQuantity += buyOrder.getQuantity();
                            System.out.println("Buyer " + buyOrder.client + " - Trade " + buyOrder.getOrderID() + " "  + buyOrder.getQuantity() + "@" + possibleOpenPrice + " - Seller " + sellOrder.client);
                            break;
                        }
                    }
                    System.out.println("==");
                }
                System.out.println("Total trade: " + tradeQuantity);
                if (tradeQuantity > highestQty) {
                    highestQty = tradeQuantity;
                    buyQueueBest = buyQueueCopy;
                    sellQueueBest = sellQueueCopy;
                }
                quantityPerPrice[quantityIndex] = tradeQuantity;
                quantityIndex++;
                System.out.println("=======");
            } else{
                System.out.println("Total trade: " + tradeQuantity);
                quantityPerPrice[quantityIndex] = tradeQuantity;
                quantityIndex++;
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
            return new TradeResult(BigDecimal.ZERO, null, null);
        }
        List<BigDecimal> uniqueBuyPricesList = new ArrayList<>(uniqueBuyPrices);
        if (marketBuyOrders > 0) {
            maxIndex++;
        }
        List<BigDecimal> sortedUniqueBuyPricesList = new ArrayList<>(sortedUniqueBuyPrices);

        List<OrderData> buyQueueBestList = new ArrayList<>(buyQueueBest);
        List<OrderData> sellQueueBestList = new ArrayList<>(sellQueueBest);

        return new TradeResult(sortedUniqueBuyPricesList.get(maxIndex), buyQueueBestList, sellQueueBestList);

    };
};
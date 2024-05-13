package Validation;

import Common.BuySell;
import Common.ClientData;
import Common.InstrumentData;
import Common.OrderData;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Validation implements Validator {
    // Used to determine position
    record ClientInstrumentPair(String clientID, String instrumentID) {}

    record InstrumentReport(BigDecimal openPrice, BigDecimal closePrice, int totalVolume, BigDecimal totalPrice, double dayHigh, double dayLow) {}

    private Map<String, ClientData> clientData;
    private Map<String, InstrumentData> instrumentData;
    private Map<ClientInstrumentPair, Integer> positionData;
    private Map<String, ValidationErrors> rejections;

    private Map<String, InstrumentReport> report;
    public Validation(List<ClientData> clientData, List<InstrumentData> instrumentData, List<OrderData> orderData) {
        this.clientData = new HashMap<>();
        this.instrumentData = new HashMap<>();
        for (var client: clientData) {
            this.clientData.put(client.clientID, client);
        }
        for (var instrument: instrumentData) {
            this.instrumentData.put(instrument.instrumentID, instrument);
        }

        this.positionData = new HashMap<>();
        this.report = new HashMap<>();
        // this.orderData = orderData;
        this.rejections = new HashMap<>();
    }

    private void log(String id, ValidationErrors error) {
        rejections.put(id, error);
    }

    /**
     * Verifies if order data is valid. Invalid order will be logged.
     */
    @Override
    public boolean verify(OrderData orderData) {
        if (!instrumentData.containsKey(orderData.instrument)) {
            log(orderData.orderID, ValidationErrors.INSTRUMENT_NOT_FOUND);
            return false;
        }
        if (!clientData.get(orderData.client).currencies.contains(instrumentData.get(orderData.instrument).currency)) {
            log(orderData.orderID, ValidationErrors.MISMATCHED_CURRENCY);
            return false;
        }
        if (orderData.quantity % instrumentData.get(orderData.instrument).lotSize != 0) {
            log(orderData.orderID, ValidationErrors.INVALID_LOT_SIZE);
            return false;
        }
        if (clientData.get(orderData.client).positionCheck && orderData.side == BuySell.Sell) {
            if (orderData.quantity > positionData.getOrDefault(new ClientInstrumentPair(orderData.client, orderData.instrument), 0)) {
                log(orderData.orderID, ValidationErrors.POSITION_CHECK_FAILED);
                return false;
            }
        }

        return true;
    }

    @Override
    public void recordTranscation(String instrument, String buyer, String seller, int quantity, BigDecimal priceTraded) {
        var buyerPair = new ClientInstrumentPair(buyer, instrument);
        var sellerPair = new ClientInstrumentPair(seller, instrument);
        var buyerPrev = positionData.getOrDefault(buyerPair, 0);
        var sellerPrev = positionData.getOrDefault(sellerPair, 0);
        positionData.put(buyerPair, buyerPrev + quantity);
        positionData.put(sellerPair, sellerPrev - quantity);

        //
        var item = report.getOrDefault(instrument, new InstrumentReport(null, null, 0, BigDecimal.ZERO, 0, 0));
        var newPrice = item.totalPrice.add(priceTraded.multiply(BigDecimal.valueOf(quantity)));
        var newVolume = item.totalVolume + quantity;
        var newDayHigh = Math.max(item.dayHigh, priceTraded.doubleValue());
        var newDayLow = Math.min(item.dayLow, priceTraded.doubleValue());
        report.put(instrument, new InstrumentReport(item.openPrice, item.closePrice, newVolume, newPrice, newDayHigh, newDayLow));
    }

    public Map<String, ValidationErrors> getRejections() {
        return rejections;
    }

    public void writeRejectionTo(String path) {
        File file = new File(path);
        try {
            FileWriter writer = new FileWriter(file);
            CSVWriter csvWriter = new CSVWriter(writer);

            for (var item: rejections.entrySet()) {
                String[] data = {item.getKey(), item.getValue().toString()};
                csvWriter.writeNext(data);
            }

            csvWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeClientTo(String path) {
        File file = new File(path);
        try {
            FileWriter writer = new FileWriter(file);
            CSVWriter csvWriter = new CSVWriter(writer);

            for (var item: positionData.entrySet()) {
                String[] data = {item.getKey().clientID, item.getKey().instrumentID, item.getValue().toString()};
                csvWriter.writeNext(data);
            }

            csvWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeInstrumentReport(String path) {
        File file = new File(path);
        try {
            FileWriter writer = new FileWriter(file);
            CSVWriter csvWriter = new CSVWriter(writer);

            for (var item: report.entrySet()) {
                String[] data = {item.getKey(), String.valueOf(item.getValue().openPrice), String.valueOf(item.getValue().closePrice), String.valueOf(item.getValue().totalVolume), String.valueOf(item.getValue().totalPrice.doubleValue() / item.getValue().totalVolume), String.valueOf(item.getValue().dayHigh), String.valueOf(item.getValue().dayLow)};
                csvWriter.writeNext(data);
            }

            csvWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

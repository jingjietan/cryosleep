package Validation;

import Common.BuySell;
import Common.ClientData;
import Common.InstrumentData;
import Common.OrderData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Validation implements Validator {
    // Used to determine position
    record ClientInstrumentPair(String clientID, String instrumentID) {}

    private Map<String, ClientData> clientData;
    private Map<String, InstrumentData> instrumentData;
    // private List<OrderData> orderData;
    private Map<ClientInstrumentPair, Integer> positionData;
    private Map<String, ValidationErrors> rejections;
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
    public void recordTranscation(OrderData orderData, int quantity) {
        int q = quantity;
        if (orderData.side == BuySell.Sell) {
            q = -q;
        }
        var cip = new ClientInstrumentPair(orderData.orderID, orderData.instrument);
        var prev = positionData.getOrDefault(cip, 0);
        positionData.replace(cip, prev + q);
    }

    public Map<String, ValidationErrors> getRejections() {
        return rejections;
    }
}

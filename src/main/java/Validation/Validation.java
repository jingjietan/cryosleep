package Validation;

import Common.ClientData;
import Common.InstrumentData;
import Common.OrderData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Validation implements Validator {
    private Map<String, ClientData> clientData;
    private Map<String, InstrumentData> instrumentData;
    private List<OrderData> orderData;
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

        this.orderData = orderData;
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
        // how to check position?
        return true;
    }

    public Map<String, ValidationErrors> getRejections() {
        return rejections;
    }
}

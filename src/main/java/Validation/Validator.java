package Validation;

import Common.OrderData;

import java.math.BigDecimal;

public interface Validator {
    boolean verify(OrderData orderData);

    // Used for position checking.
    void recordTranscation(String instrument, String buyer, String seller, int quantity, BigDecimal priceTraded);
}

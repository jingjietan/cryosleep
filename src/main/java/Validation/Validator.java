package Validation;

import Common.OrderData;

public interface Validator {
    boolean verify(OrderData orderData);

    // Used for position checking.
    void recordTranscation(OrderData orderData, int quantity);
}

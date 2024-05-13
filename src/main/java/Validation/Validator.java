package Validation;

import Common.OrderData;

public interface Validator {
    boolean verify(OrderData orderData);
}

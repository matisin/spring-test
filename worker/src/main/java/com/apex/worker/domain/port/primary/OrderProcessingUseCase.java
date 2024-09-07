package com.apex.worker.domain.port.primary;

import java.util.ArrayList;

public interface OrderProcessingUseCase {
    boolean processOrder(String orderId, String customerId, ArrayList<String> productIds);
}

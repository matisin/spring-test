package com.apex.worker.domain.port.secondary;

import com.apex.worker.domain.entity.Order;

public interface OrderRepository {
    boolean save(Order order);
}

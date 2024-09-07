package com.apex.worker.application;

import java.time.Duration;
import java.util.ArrayList;

import com.apex.worker.domain.entity.Customer;
import com.apex.worker.domain.entity.Order;
import com.apex.worker.domain.entity.Product;
import com.apex.worker.domain.port.primary.OrderProcessingUseCase;
import com.apex.worker.domain.port.secondary.OrderRepository;
import com.apex.worker.domain.port.secondary.ProductService;
import com.apex.worker.domain.port.secondary.RedisService;
import com.apex.worker.domain.port.secondary.CustomerService;

public class OrderProcessingService implements OrderProcessingUseCase {

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final RedisService redisService;
    private final CustomerService customerService;

    public OrderProcessingService(OrderRepository orderRepository, ProductService productService,
            CustomerService customerService, RedisService redisService) {
        this.orderRepository = orderRepository;
        this.productService = productService;
        this.customerService = customerService;
        this.redisService = redisService;
    }

    @Override
    public boolean processOrder(String orderId, String customerId, ArrayList<String> productIds) {
        boolean acquired = redisService.acquireLock(orderId, Duration.ofSeconds(1));
        if (!acquired) {
            throw new IllegalStateException("Lock could not be acquired");
        }
        int count = redisService.incrementRetryCount(orderId);
        if (count > 3) {
            redisService.storeError(orderId, "Max retries reached");
            redisService.releaseLock(orderId);
            return false;
        }

        if(!customerService.isValidCustomer(customerId)) {
            redisService.storeError(orderId, "Invalid customer");
            redisService.releaseLock(orderId);
            throw new IllegalArgumentException("Invalid customer");
        }
        Customer customer = customerService.getCustomerDetails(customerId);

        ArrayList<Product> validProducts = this.productService.getProducts(productIds);
        if (validProducts.isEmpty()) {
            redisService.storeError(orderId, "Order must contain at least one valid product");
            redisService.releaseLock(orderId);
            throw new IllegalArgumentException("Order must contain at least one valid product");
        }

        Order order = new Order(orderId, customerId, customer.getName(), validProducts);
        order.setProducts(validProducts);
        order.setCustomerId(customerId);
        order.setCustomerName(customer.getName());
        boolean savedOrder = orderRepository.save(order);

        redisService.releaseLock(orderId);
        redisService.clearOrderData(orderId);

        return savedOrder;
    }

    // private double calculateTotalAmount(List<Product> products) {
    // return products.stream().mapToDouble(Product::getPrice).sum();
    // }
}

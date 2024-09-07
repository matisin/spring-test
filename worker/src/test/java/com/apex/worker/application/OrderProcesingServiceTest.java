package com.apex.worker.application;

import com.apex.worker.domain.entity.Customer;
import com.apex.worker.domain.entity.Order;
import com.apex.worker.domain.entity.Product;
import com.apex.worker.domain.port.secondary.OrderRepository;
import com.apex.worker.domain.port.secondary.ProductService;
import com.apex.worker.domain.port.secondary.CustomerService;
import com.apex.worker.domain.port.secondary.RedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class OrderProcessingServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductService productService;
    @Mock
    private CustomerService customerService;
    @Mock
    private RedisService redisService;

    private OrderProcessingService orderProcessingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        orderProcessingService = new OrderProcessingService(orderRepository, productService, customerService,
                redisService);
    }

    @Test
    void processOrder_successfulProcessing() {
        Product product = new Product("product1", "product name", "description", new BigDecimal("100.00"), 1);
        ArrayList<Product> products = new ArrayList<Product>();
        products.add(product);

        ArrayList<String> productIds = new ArrayList<String>();
        productIds.add("product1");

        when(redisService.acquireLock(anyString(), any(Duration.class))).thenReturn(true);
        when(redisService.incrementRetryCount(anyString())).thenReturn(1);
        when(customerService.getCustomerDetails("customer1"))
                .thenReturn(new Customer("customer1", "name", "email", "address", true));
        when(productService.getProducts(productIds)).thenReturn(products);
        when(customerService.isValidCustomer("customer1")).thenReturn(true);
        when(orderRepository.save(any())).thenReturn(true);

        orderProcessingService.processOrder("order1", "customer1", productIds);

        // verify(orderRepository).save(order);
        // assertEquals("name", order.getCustomerName());
        // assertEquals("product name", order.getProducts().get(0).getName());
        // assertEquals(new BigDecimal("101.00"), order.getProducts().get(0).getPrice());
        verify(redisService).clearOrderData("order1");
        verify(redisService).releaseLock("order1");
    }

    @Test
    void processOrder_lockAcquisitionFailure() {
        // Arrange
        ArrayList<String> productIds = new ArrayList<String>();
        productIds.add("product1");

        when(redisService.acquireLock(anyString(), any(Duration.class))).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> orderProcessingService.processOrder("order1", "customer1", productIds));
        verify(redisService, never()).incrementRetryCount(anyString());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void processOrder_maxRetryCountExceeded() {
        ArrayList<String> productIds = new ArrayList<String>();
        productIds.add("product1");
        // Arrange
        when(redisService.acquireLock(anyString(), any(Duration.class))).thenReturn(true);
        when(redisService.incrementRetryCount(anyString())).thenReturn(4);

        // Act
        orderProcessingService.processOrder("order1", "customer1", productIds);

        // Assert
        verify(redisService).storeError(eq("order1"), anyString());
        verify(orderRepository, never()).save(any(Order.class));
        verify(redisService).releaseLock("order1");
    }

    @Test
    void processOrder_exceptionDuringProcessing() {
        // Arrange
        ArrayList<String> productIds = new ArrayList<String>();
        productIds.add("product1");

        when(redisService.acquireLock(anyString(), any(Duration.class))).thenReturn(true);
        when(redisService.incrementRetryCount(anyString())).thenReturn(1);
        when(customerService.getCustomerDetails("customer1")).thenThrow(new RuntimeException("API Error"));

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> orderProcessingService.processOrder("order1", "customer1", productIds));
        verify(redisService).storeError(eq("order1"), anyString());
        verify(orderRepository, never()).save(any(Order.class));
        verify(redisService).releaseLock("order1");
    }
}

package com.apex.worker.integration;

import com.apex.worker.WorkerApplication;
import com.apex.worker.domain.entity.Customer;
import com.apex.worker.domain.entity.Order;
import com.apex.worker.domain.entity.Product;
import com.apex.worker.domain.port.secondary.OrderRepository;
import com.apex.worker.domain.port.secondary.RedisService;
import com.apex.worker.domain.port.secondary.ProductService;
import com.apex.worker.domain.port.secondary.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.awaitility.Awaitility.await;

@SpringBootTest(classes = WorkerApplication.class)
@Testcontainers
class OrderProcessingIntegrationTest {

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"));

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:latest"));

    @Container
    static GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:latest"))
            .withExposedPorts(6379);

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RedisService redisService;

    @MockBean
    private ProductService productService;

    @MockBean
    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        Product product = new Product("product1", "product name", "description", new BigDecimal("100.00"), 1);
        ArrayList<Product> products = new ArrayList<Product>();
        products.add(product);

        ArrayList<String> productIds = new ArrayList<String>();
        productIds.add("product1");

        when(customerService.getCustomerDetails("customer1"))
                .thenReturn(new Customer("customer1", "name", "email", "address", true));
        when(productService.getProducts(productIds)).thenReturn(products);
        when(customerService.isValidCustomer("customer1")).thenReturn(true);
        when(orderRepository.save(any())).thenReturn(true);
    }

    @DynamicPropertySource
    static void registerKafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Test
    void testOrderProcessingIntegration() throws Exception {
        // Arrange
        String orderId = "test-order-1";
        String orderJson = "{\"orderId\":\"" + orderId
                + "\",\"customerId\":\"customer1\",\"products\":[{\"productId\":\"product1\"}]}";

        // Act
        kafkaTemplate.send("orders", orderJson);

        // Assert
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Order processedOrder = orderRepository.findById(orderId).orElse(null);
            assert processedOrder != null;
            assert "customer1".equals(processedOrder.getCustomerId());
            assert "John Doe".equals(processedOrder.getCustomerName());
            assert processedOrder.getProducts().size() == 1;
            assert "product1".equals(processedOrder.getProducts().get(0).getProductId());
            assert "Enriched product1".equals(processedOrder.getProducts().get(0).getName());
            assert new BigDecimal("10.00").equals(processedOrder.getProducts().get(0).getPrice());
        });

        verify(redisLockService).acquireLock(any(), any());
        verify(redisLockService).releaseLock(any());
        verify(customerService).getCustomerName("customer1");
        verify(productService).enrichProductData(any(Product.class));
    }

    // Puedes añadir más pruebas de integración aquí...
}

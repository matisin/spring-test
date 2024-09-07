package e2e

import (
	"context"
	"encoding/json"
	"testing"
	"time"

	"github.com/segmentio/kafka-go"
	"github.com/stretchr/testify/assert"
	"go.mongodb.org/mongo-driver/bson"
)

func TestSuccessfulOrderProcessing(t *testing.T) {
	writer := getKafkaWriter()
	defer writer.Close()

	// Preparar el pedido
	order := map[string]interface{}{
		"orderId":    "order-123",
		"customerId": "customer-456",
		"products":   []string{"product-789"},
	}

	// Enviar el pedido a Kafka
	orderJSON, _ := json.Marshal(order)
	err := writer.WriteMessages(context.Background(), kafka.Message{Value: orderJSON})
	assert.NoError(t, err, "Failed to write message to Kafka")

	// Esperar a que el worker procese el pedido
	time.Sleep(5 * time.Second)

	// Verificar que el pedido se haya procesado y almacenado en MongoDB
	client, err := getMongoClient()
	assert.NoError(t, err, "Failed to connect to MongoDB")
	defer client.Disconnect(context.Background())

	collection := client.Database(dbName).Collection(collName)
	var processedOrder Order
	err = collection.FindOne(context.Background(), bson.M{"orderId": "order-123"}).Decode(&processedOrder)
	assert.NoError(t, err, "Failed to find processed order in MongoDB")

	// Verificar los datos del pedido procesado
	assert.Equal(t, "order-123", processedOrder.OrderID)
	assert.Equal(t, "customer-456", processedOrder.CustomerID)
	assert.NotEmpty(t, processedOrder.CustomerName, "Customer name should not be empty")
	assert.Len(t, processedOrder.Products, 1, "Should have one product")
	assert.Equal(t, "product-789", processedOrder.Products[0].ProductID)
	assert.NotEmpty(t, processedOrder.Products[0].Name, "Product name should not be empty")
	assert.Greater(t, processedOrder.Products[0].Price, 0.0, "Product price should be greater than 0")
}

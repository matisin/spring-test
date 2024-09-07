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

func TestNonExistentProduct(t *testing.T) {
	writer := getKafkaWriter()
	defer writer.Close()

	// Preparar el pedido con un producto no existente
	order := map[string]interface{}{
		"orderId":    "order-error-1",
		"customerId": "customer-456",
		"products":   []string{"non-existent-product"},
	}

	// Enviar el pedido a Kafka
	orderJSON, _ := json.Marshal(order)
	err := writer.WriteMessages(context.Background(), kafka.Message{Value: orderJSON})
	assert.NoError(t, err, "Failed to write message to Kafka")

	// Esperar a que el worker procese el pedido
	time.Sleep(5 * time.Second)

	// Verificar que el pedido no se haya procesado
	client, err := getMongoClient()
	assert.NoError(t, err, "Failed to connect to MongoDB")
	defer client.Disconnect(context.Background())

	collection := client.Database(dbName).Collection(collName)
	var processedOrder Order
	err = collection.FindOne(context.Background(), bson.M{"orderId": "order-error-1"}).Decode(&processedOrder)
	assert.Error(t, err, "Order with non-existent product should not be processed")
}

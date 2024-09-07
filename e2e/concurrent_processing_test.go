package e2e

import (
	"context"
	"encoding/json"
	"fmt"
	"sync"
	"testing"
	"time"

	"github.com/segmentio/kafka-go"
	"github.com/stretchr/testify/assert"
	"go.mongodb.org/mongo-driver/bson"
)

func TestConcurrentOrderProcessing(t *testing.T) {
	writer := getKafkaWriter()
	defer writer.Close()

	numOrders := 10
	var wg sync.WaitGroup
	wg.Add(numOrders)

	for i := 0; i < numOrders; i++ {
		go func(orderNum int) {
			defer wg.Done()
			order := map[string]interface{}{
				"orderId":    fmt.Sprintf("order-concurrent-%d", orderNum),
				"customerId": "customer-456",
				"products":   []string{"product-789"},
			}
			orderJSON, _ := json.Marshal(order)
			err := writer.WriteMessages(context.Background(), kafka.Message{Value: orderJSON})
			assert.NoError(t, err, "Failed to write message to Kafka")
		}(i)
	}

	wg.Wait()

	// Esperar a que el worker procese los pedidos
	time.Sleep(10 * time.Second)

	// Verificar que todos los pedidos se hayan procesado
	client, err := getMongoClient()
	assert.NoError(t, err, "Failed to connect to MongoDB")
	defer client.Disconnect(context.Background())

	collection := client.Database(dbName).Collection(collName)

	for i := 0; i < numOrders; i++ {
		var processedOrder Order
		err := collection.FindOne(context.Background(), bson.M{"orderId": fmt.Sprintf("order-concurrent-%d", i)}).Decode(&processedOrder)
		assert.NoError(t, err, fmt.Sprintf("Failed to find processed order %d in MongoDB", i))
	}
}

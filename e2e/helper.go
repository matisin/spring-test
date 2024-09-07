package e2e

import (
	"context"
	"time"

	"github.com/segmentio/kafka-go"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
)

const (
	kafkaAddress = "localhost:9092"
	mongoURI     = "mongodb://localhost:27017"
	ordersTopic  = "orders"
	dbName       = "orders"
	collName     = "processed_orders"
)

type Order struct {
	OrderID      string    `bson:"orderId"`
	CustomerID   string    `bson:"customerId"`
	CustomerName string    `bson:"customerName"`
	Products     []Product `bson:"products"`
}

type Product struct {
	ProductID string  `bson:"productId"`
	Name      string  `bson:"name"`
	Price     float64 `bson:"price"`
}

func getKafkaWriter() *kafka.Writer {
	return kafka.NewWriter(kafka.WriterConfig{
		Brokers: []string{kafkaAddress},
		Topic:   ordersTopic,
	})
}

func getMongoClient() (*mongo.Client, error) {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()
	return mongo.Connect(ctx, options.Client().ApplyURI(mongoURI))
}

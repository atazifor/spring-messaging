# Asynchronous Event-Driven Communication with Spring Cloud Stream

## Overview

This project demonstrates how to implement **asynchronous event-driven communication** between microservices using **Spring Cloud Stream**, with **Kafka** and **RabbitMQ** as message brokers. The tutorial provides a practical example of building a **producer-consumer system** leveraging Docker containers for easy setup and scalability.

We use **multi-binder support** to work with both Kafka and RabbitMQ. The setup includes **dynamic profile switching**, allowing you to choose between Kafka and RabbitMQ at runtime. The infrastructure is containerized using Docker, ensuring an easy way to start all necessary services.

### Key Technologies
- **Spring Boot** with **Spring Cloud Stream**
- **Apache Kafka** (using `confluentinc/cp-kafka` Docker image)
- **RabbitMQ** (using `rabbitmq:3-management` Docker image)
- **Docker and Docker Compose**
- **Profiles for Kafka and RabbitMQ** for binder flexibility

## Project Structure

### Application Configuration
The application uses **Spring Cloud Stream** to facilitate the communication between services by defining **input** and **output bindings** in the `application.yml` file. The `application.yml` includes configurations for both Kafka and RabbitMQ as binders.

**`application.yml` Configuration:**
```yaml
spring:
  cloud:
    stream:
      bindings:
        invoiceInput:
          destination: invoice-topic
          group: invoice-group
        orderInput:
          destination: order-topic
          group: order-group

        invoiceOutput:
          destination: invoice-topic
        orderOutput:
          destination: order-topic
      kafka:
        binder:
          brokers: kafka:9092
      default-binder: kafka
# Profile for RabbitMQ (activated when using the 'rabbitmq' profile)
---
spring:
  profiles:
    active: rabbitmq
  cloud:
    stream:
      bindings:
        customerInput:
          destination: customer-queue
        orderInput:
          destination: order-queue
        invoiceOutput:
          destination: invoice-queue
        paymentOutput:
          destination: payment-queue

      rabbit:
        binder:
          hosts: rabbitmq
          port: 5672
          username: guest
          password: guest
```

### Docker Compose Setup
The `docker-compose.yml` file defines all services required to run the system: **Kafka**, **Zookeeper**, **RabbitMQ**, and the **Spring Boot application**. Each service is configured with appropriate health checks to ensure that services are only started once dependencies are ready.

**`docker-compose.yml` Configuration:**
```yaml
services:
  zookeeper:
    image: zookeeper:3.8
    ports:
      - "2181:2181"
    healthcheck:
      test: ["CMD", "echo", "ruok", "|", "nc", "localhost", "2181"]
      interval: 30s
      timeout: 10s
      retries: 5

  kafka:
    image: confluentinc/cp-kafka:7.3.1
    restart: always
    mem_limit: 1024m
    ports:
      - "9092:9092"
    environment:
      - KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181
      - KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://kafka:9092
      - KAFKA_BROKER_ID=1
      - KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1
    depends_on:
      zookeeper:
        condition: service_healthy
    healthcheck:
      test: [ "CMD-SHELL", "kafka-broker-api-versions --bootstrap-server localhost:9092 || exit 1" ]
      interval: 30s
      timeout: 10s
      retries: 5

  rabbitmq:
    image: rabbitmq:3-management
    ports:
      - "5672:5672"
      - "15672:15672" # Management UI
    healthcheck:
      test: ["CMD", "rabbitmqctl", "status"]
      interval: 30s
      timeout: 10s
      retries: 5

  app:
    build:
      context: .
    ports:
      - "8080:8080"
    depends_on:
      kafka:
        condition: service_healthy
      rabbitmq:
        condition: service_healthy
    environment:
      SPRING_PROFILES_ACTIVE: kafka # Use 'rabbitmq' to switch to RabbitMQ
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 5
```

### Important Insights and Troubleshooting

- **Matching Topic Names:** It is essential that the **input channel topic names** match the **output channel destination names**. Even though binding names can be different, the destination (e.g., topic or queue name) must be consistent between the producer and consumer.

- **Docker Container Networking:** When connecting between different Docker containers, **`localhost`** cannot be used. Instead, you need to use the **service name** defined in the `docker-compose.yml` file (e.g., `kafka`, `rabbitmq`) to correctly route messages between services.

- **Health Checks and Dependencies:** Kafka and RabbitMQ services have **health checks** defined in `docker-compose.yml` to ensure that the Spring Boot application starts only after Kafka and RabbitMQ are ready. This helps avoid connection issues like **timeout exceptions**.

## Running the Application

1. **Clone the Repository**: Clone the repository containing this project.
   ```sh
   git clone <repository-url>
   cd <repository-directory>
   ```

2. **Start Docker Containers**: Use Docker Compose to start all services.
   ```sh
   docker-compose up --build
   ```
   This command will build the Docker image for the Spring Boot application and start all the services, ensuring that **Zookeeper**, **Kafka**, **RabbitMQ**, and your **application** are properly started in the right order.

3. **Testing the Setup**:
    - Access the **RabbitMQ Management UI** at `http://localhost:15672` (default credentials: **guest/guest**).
    - Use **Postman** or **cURL** to send messages to your REST endpoints, and observe the consumer logs.

4. **Switching Binders**:
    - By default, the profile is set to **Kafka** (`SPRING_PROFILES_ACTIVE: kafka`). To switch to **RabbitMQ**, update the `docker-compose.yml` to:
      ```yaml
      environment:
        SPRING_PROFILES_ACTIVE: rabbitmq
      ```
    - Restart the containers to apply changes.

## Sample Producer and Consumer Functions

**Producer Example (Using StreamBridge):**
```java
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageProducer {
    private final StreamBridge streamBridge;

    public MessageProducer(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    @PostMapping("/sendInvoice")
    public String sendInvoice(@RequestBody String message) {
        streamBridge.send("invoiceOutput", message);
        return "Invoice message sent: " + message;
    }
}
```

**Consumer Example:**
```java
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
public class InvoiceConsumer {
    @Bean
    public Consumer<String> invoiceInput() {
        return message -> {
            System.out.println("Received invoice message: " + message);
        };
    }
}
```

## Summary
This tutorial demonstrates how to set up an **event-driven architecture** using **Spring Cloud Stream** with both **Kafka** and **RabbitMQ** binders. By leveraging **Docker** for running **Kafka**, **RabbitMQ**, and the Spring Boot application, it becomes easy to replicate and test the environment. This setup also highlights important troubleshooting tips, such as correctly matching input and output destinations and using proper Docker networking configurations.

Feel free to explore, modify, and experiment with different profiles and configurations to get a better understanding of **asynchronous communication** in microservice architectures.


# Firefly Framework - Event-Driven Architecture (EDA)

[![CI](https://github.com/fireflyframework/fireflyframework-eda/actions/workflows/ci.yml/badge.svg)](https://github.com/fireflyframework/fireflyframework-eda/actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-21%2B-orange.svg)](https://openjdk.org)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green.svg)](https://spring.io/projects/spring-boot)

> Unified event-driven architecture library with Kafka, RabbitMQ, and Spring Application Events support.

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Requirements](#requirements)
- [Installation](#installation)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
- [Documentation](#documentation)
- [Contributing](#contributing)
- [License](#license)

## Overview

Firefly Framework EDA provides a standardized messaging abstraction for event-driven architectures, supporting multiple broker implementations through a unified publisher/consumer API. It enables reactive event publishing and consumption with built-in support for Apache Kafka, RabbitMQ, and Spring Application Events as transport mechanisms.

The library features annotation-driven event publishing (`@EventPublisher`, `@PublishResult`), declarative event listeners (`@EventListener`), and a comprehensive set of event filtering, serialization, and error handling capabilities. It includes support for JSON, Avro, and Protobuf message serialization formats.

The resilient publisher wrapper provides circuit breaker integration, while the dead letter queue handler ensures no events are lost during processing failures. Metrics collection and health indicators provide full observability into the messaging infrastructure.

## Features

- Multi-broker support: Apache Kafka, RabbitMQ, Spring Application Events
- Annotation-driven publishing: `@EventPublisher`, `@PublishResult`
- Declarative event listening: `@EventListener` with SpEL-based filtering
- Event envelope pattern with metadata propagation
- Pluggable serialization: JSON, Avro, Protobuf
- Event filtering: type-based, header-based, destination-based, composite
- Dead letter queue (DLQ) handling for failed messages
- Resilient publisher with circuit breaker support
- Dynamic event listener registration at runtime
- AMQP admin auto-configuration for RabbitMQ exchanges and queues
- Custom error handling strategies with metrics and notification handlers
- Health indicators and metrics for Actuator integration
- Spring Boot auto-configuration for Kafka and RabbitMQ

## Requirements

- Java 21+
- Spring Boot 3.x
- Maven 3.9+
- Apache Kafka or RabbitMQ (depending on chosen broker)

## Installation

```xml
<dependency>
    <groupId>org.fireflyframework</groupId>
    <artifactId>fireflyframework-eda</artifactId>
    <version>26.01.01</version>
</dependency>
```

## Quick Start

```java
import org.fireflyframework.eda.annotation.EventPublisher;
import org.fireflyframework.eda.annotation.EventListener;
import org.fireflyframework.eda.event.EventEnvelope;

@Service
public class OrderService {

    @EventPublisher(topic = "orders")
    public Mono<OrderCreatedEvent> createOrder(OrderRequest request) {
        // Business logic - return value is automatically published
        return Mono.just(new OrderCreatedEvent(request.getId()));
    }
}

@Component
public class OrderEventHandler {

    @EventListener(topic = "orders")
    public Mono<Void> onOrderCreated(EventEnvelope<OrderCreatedEvent> envelope) {
        // Handle the event
        return processOrder(envelope.getPayload());
    }
}
```

## Configuration

```yaml
firefly:
  eda:
    broker: kafka  # kafka, rabbitmq, spring
    kafka:
      bootstrap-servers: localhost:9092
      consumer:
        group-id: my-service
    rabbitmq:
      host: localhost
      port: 5672
```

## Documentation

Additional documentation is available in the [docs/](docs/) directory:

- [Quickstart](docs/QUICKSTART.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Configuration](docs/CONFIGURATION.md)
- [Annotations](docs/ANNOTATIONS.md)
- [Api Reference](docs/API_REFERENCE.md)
- [Publisher Types](docs/PUBLISHER_TYPES.md)
- [Dynamic Topic Selection](docs/DYNAMIC_TOPIC_SELECTION.md)
- [Eventlistener Examples](docs/EVENTLISTENER_EXAMPLES.md)
- [Custom Error Handling](docs/CUSTOM_ERROR_HANDLING.md)
- [Examples](docs/EXAMPLES.md)
- [Developer Guide](docs/DEVELOPER_GUIDE.md)
- [Contributing](docs/CONTRIBUTING.md)

## Contributing

Contributions are welcome. Please read the [CONTRIBUTING.md](CONTRIBUTING.md) guide for details on our code of conduct, development process, and how to submit pull requests.

## License

Copyright 2024-2026 Firefly Software Solutions Inc.

Licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE) for details.

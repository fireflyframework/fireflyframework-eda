# Firefly Framework - Event-Driven Architecture (EDA)

[![CI](https://github.com/fireflyframework/fireflyframework-eda/actions/workflows/ci.yml/badge.svg)](https://github.com/fireflyframework/fireflyframework-eda/actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-21%2B-orange.svg)](https://openjdk.org)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green.svg)](https://spring.io/projects/spring-boot)

> Unified event-driven architecture library with Kafka, RabbitMQ, PostgreSQL (LISTEN/NOTIFY + outbox), and Spring Application Events support.

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

Firefly Framework EDA provides a standardized messaging abstraction for event-driven architectures, supporting multiple broker implementations through a unified publisher/consumer API. It enables reactive event publishing and consumption with built-in support for Apache Kafka, RabbitMQ, PostgreSQL (via `LISTEN`/`NOTIFY` backed by a transactional outbox table), and Spring Application Events as transport mechanisms.

The library features annotation-driven event publishing (`@EventPublisher`, `@PublishResult`), declarative event listeners (`@EventListener`), and a comprehensive set of event filtering, serialization, and error handling capabilities. It includes support for JSON, Avro, and Protobuf message serialization formats.

The resilient publisher wrapper provides circuit breaker integration, while the dead letter queue handler ensures no events are lost during processing failures. Metrics collection and health indicators provide full observability into the messaging infrastructure.

## Features

- Multi-broker support: Apache Kafka, RabbitMQ, PostgreSQL (`LISTEN`/`NOTIFY` + outbox), Spring Application Events
- Annotation-driven publishing: `@EventPublisher`, `@PublishResult`
- Declarative event listening: `@EventListener` with SpEL-based filtering
- Event envelope pattern with metadata propagation
- Pluggable serialization: JSON, Avro, Protobuf
- Event filtering: type-based, header-based, destination-based, composite
- Dead letter queue (DLQ) handling for failed messages
- Resilient publisher with circuit breaker support
- Dynamic event listener registration at runtime
- AMQP admin auto-configuration for RabbitMQ exchanges and queues
- Transactional outbox table for PostgreSQL with auto-created schema and trigger
- Custom error handling strategies with metrics and notification handlers
- Health indicators and metrics for Actuator integration
- Spring Boot auto-configuration for Kafka, RabbitMQ, and PostgreSQL

## Requirements

- Java 21+
- Spring Boot 3.x
- Maven 3.9+
- Apache Kafka, RabbitMQ, or PostgreSQL 11+ (depending on chosen transport)

## Installation

```xml
<dependency>
    <groupId>org.fireflyframework</groupId>
    <artifactId>fireflyframework-eda</artifactId>
    <version>26.02.07</version>
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
    enabled: true
    default-publisher-type: AUTO   # AUTO chooses KAFKA → RABBITMQ → POSTGRES → APPLICATION_EVENT
    publishers:
      enabled: true
      kafka:
        default:
          enabled: true
          bootstrap-servers: localhost:9092
      rabbitmq:
        default:
          enabled: true
          host: localhost
          port: 5672
      postgres:
        default:
          enabled: true
          host: localhost
          port: 5432
          database: app
          username: app
          password: secret
          schema: public
          outbox-table: firefly_eda_outbox
          default-destination: events
          auto-create-schema: true   # provision outbox table + NOTIFY trigger at startup
    consumer:
      enabled: true
      group-id: my-service
      kafka:
        default:
          enabled: true
          bootstrap-servers: localhost:9092
      rabbitmq:
        default:
          enabled: true
          host: localhost
          port: 5672
          queues: events-queue
      postgres:
        default:
          enabled: true
          host: localhost
          port: 5432
          database: app
          username: app
          password: secret
          channels: events,order-events   # destinations to LISTEN on
          polling-interval: 30s           # NOTIFY-loss fallback poll cadence
          max-attempts: 3                 # outbox row moves to DEAD_LETTER after N failures
```

### PostgreSQL transport at a glance

- Each `publish()` performs a single `INSERT` into `firefly_eda_outbox`. A
  database trigger fires `pg_notify(channel, id)` for every inserted row.
- The consumer holds a dedicated R2DBC connection that runs `LISTEN <channel>`
  for every subscribed destination. Notifications carry only the outbox row
  id so payloads can be arbitrarily large.
- On dispatch, the listener pipeline either marks the row `PROCESSED` or
  increments `attempts`; once `attempts` reaches `max-attempts`, the row
  moves to `DEAD_LETTER` status.
- A periodic poll (`polling-interval`) catches rows that slipped past the
  live channel (e.g., consumer offline at insert time, payload too large,
  connection reset). Set it to `0s` to disable polling.
- Channel names are derived deterministically from destinations via the
  built-in mapper: non-alphanumeric characters become `_`, the result is
  lower-cased, prefixed with `firefly_eda_`, and truncated to fit
  PostgreSQL's 63-byte identifier limit with a stable hash suffix when
  needed.

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

/*
 * Copyright 2024-2026 Firefly Software Solutions Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fireflyframework.eda.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Environment post-processor that conditionally excludes Spring Boot's
 * RabbitMQ and Kafka auto-configurations when the corresponding Firefly EDA
 * transports are not enabled.
 *
 * <p>This prevents Spring Boot from auto-creating connection factories and
 * health indicators for messaging systems that the application does not use,
 * avoiding startup errors like "Rabbit health check failed - Connection refused".
 *
 * <p><strong>Behavior:</strong>
 * <ul>
 *   <li>If neither {@code firefly.eda.publishers.rabbitmq.default.enabled} nor
 *       {@code firefly.eda.consumer.rabbitmq.default.enabled} is {@code true},
 *       and {@code spring.rabbitmq.host} is not explicitly set,
 *       then {@code RabbitAutoConfiguration} is excluded.</li>
 *   <li>If neither {@code firefly.eda.publishers.kafka.default.enabled} nor
 *       {@code firefly.eda.consumer.kafka.default.enabled} is {@code true},
 *       and {@code spring.kafka.bootstrap-servers} is not explicitly set,
 *       then {@code KafkaAutoConfiguration} is excluded.</li>
 * </ul>
 *
 * <p>Any existing {@code spring.autoconfigure.exclude} entries are preserved.
 */
public class FireflyEdaEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String EXCLUDE_PROPERTY = "spring.autoconfigure.exclude";
    private static final String RABBIT_AUTO_CONFIG = "org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration";
    private static final String KAFKA_AUTO_CONFIG = "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        List<String> excludes = new ArrayList<>();

        if (shouldExcludeRabbit(environment)) {
            excludes.add(RABBIT_AUTO_CONFIG);
        }

        if (shouldExcludeKafka(environment)) {
            excludes.add(KAFKA_AUTO_CONFIG);
        }

        if (!excludes.isEmpty()) {
            applyExcludes(environment, excludes);
        }
    }

    private boolean shouldExcludeRabbit(ConfigurableEnvironment env) {
        // Respect explicit Spring Boot native configuration
        if (env.containsProperty("spring.rabbitmq.host")) {
            return false;
        }

        boolean publisherEnabled = env.getProperty(
                "firefly.eda.publishers.rabbitmq.default.enabled", Boolean.class, false);
        boolean consumerEnabled = env.getProperty(
                "firefly.eda.consumer.rabbitmq.default.enabled", Boolean.class, false);

        return !publisherEnabled && !consumerEnabled;
    }

    private boolean shouldExcludeKafka(ConfigurableEnvironment env) {
        // Respect explicit Spring Boot native configuration
        if (env.containsProperty("spring.kafka.bootstrap-servers")) {
            return false;
        }

        boolean publisherEnabled = env.getProperty(
                "firefly.eda.publishers.kafka.default.enabled", Boolean.class, false);
        boolean consumerEnabled = env.getProperty(
                "firefly.eda.consumer.kafka.default.enabled", Boolean.class, false);

        return !publisherEnabled && !consumerEnabled;
    }

    private void applyExcludes(ConfigurableEnvironment environment, List<String> newExcludes) {
        // Read existing excludes to preserve them
        String existing = environment.getProperty(EXCLUDE_PROPERTY, "");
        List<String> allExcludes = new ArrayList<>();

        if (!existing.isEmpty()) {
            allExcludes.addAll(
                    Arrays.stream(existing.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toList()));
        }

        // Add new excludes (avoiding duplicates)
        for (String exclude : newExcludes) {
            if (!allExcludes.contains(exclude)) {
                allExcludes.add(exclude);
            }
        }

        Map<String, Object> props = new HashMap<>();
        props.put(EXCLUDE_PROPERTY, String.join(",", allExcludes));

        environment.getPropertySources().addFirst(
                new MapPropertySource("fireflyEdaAutoConfigExcludes", props));
    }
}

package com.sorz.rabbitmqproducerspring.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String QUEUE_NAME = "exampleQueue";

    @Bean
    public Queue exampleQueue() {
        // false indica que la cola no es durable (no se mantiene después de reiniciar RabbitMQ)
        return new Queue(QUEUE_NAME, false);
    }
}

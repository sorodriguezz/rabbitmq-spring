package com.sorz.rabbitmqconsumerspring.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String QUEUE_NAME = "exampleQueue";

    @Bean
    public Queue exampleQueue() {
        return new Queue(QUEUE_NAME, false);
    }

    @RabbitListener(queues = QUEUE_NAME)
    public void listen(String message) {
        // Se ejecuta cada vez que se recibe un mensaje en la cola
        System.out.println("Mensaje recibido: " + message);
    }
}

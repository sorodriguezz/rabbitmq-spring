package com.sorz.rabbitmqproducerspring.service;

import com.sorz.rabbitmqproducerspring.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class MessageProducer {
    private final RabbitTemplate rabbitTemplate;

    public MessageProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMessage(String message) {
        // Envía el mensaje a la cola "exampleQueue"
        this.rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_NAME, message);
    }
}

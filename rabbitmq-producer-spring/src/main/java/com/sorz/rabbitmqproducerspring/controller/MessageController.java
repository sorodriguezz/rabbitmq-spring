package com.sorz.rabbitmqproducerspring.controller;

import com.sorz.rabbitmqproducerspring.service.MessageProducer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageController {
    private final MessageProducer messageProducer;

    public MessageController(MessageProducer messageProducer) {
        this.messageProducer = messageProducer;
    }

    @GetMapping("send")
    public String sendMessage(@RequestParam String message) {
        messageProducer.sendMessage(message);
        return "Mensaje enviado: " + message;
    }
}

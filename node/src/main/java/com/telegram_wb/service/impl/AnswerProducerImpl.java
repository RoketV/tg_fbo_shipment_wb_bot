package com.telegram_wb.service.impl;

import com.telegram_wb.service.AnswerProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Service
@RequiredArgsConstructor
public class AnswerProducerImpl implements AnswerProducer {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void produce(String rabbitQueue, SendMessage sendMessage) {
        rabbitTemplate.convertAndSend(rabbitQueue, sendMessage);
    }

    @Override
    public void produce(String rabbitQueue, SendDocument sendDocument) {
        rabbitTemplate.convertAndSend(rabbitQueue, sendDocument);
    }
}

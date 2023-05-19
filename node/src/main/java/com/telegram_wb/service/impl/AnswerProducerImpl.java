package com.telegram_wb.service.impl;

import com.telegram_wb.dto.DocumentDto;
import com.telegram_wb.service.AnswerProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnswerProducerImpl implements AnswerProducer {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void produce(String rabbitQueue, SendMessage sendMessage) {
        log.info("Node sent answer {} to dispatcher", sendMessage.getText());
        rabbitTemplate.convertAndSend(rabbitQueue, sendMessage);
    }

    @Override
    public void produce(String rabbitQueue, DocumentDto documentDto) {
        log.info("Node sent documentDto to dispatcher");
        rabbitTemplate.convertAndSend(rabbitQueue, documentDto);
    }
}

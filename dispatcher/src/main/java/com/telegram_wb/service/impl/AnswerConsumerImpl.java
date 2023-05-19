package com.telegram_wb.service.impl;

import com.telegram_wb.controller.UpdateController;
import com.telegram_wb.dto.DocumentDto;
import com.telegram_wb.service.AnswerConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import static com.telegram_wb.rabbitmq.RabbitQueues.*;


@Service
@Slf4j
@RequiredArgsConstructor
public class AnswerConsumerImpl implements AnswerConsumer {

    private final UpdateController updateController;

    @Override
    @RabbitListener(queues = TEXT_ANSWER)
    public void consume(SendMessage sendMessage) {
        log.info("received {} from node in dispatcher", TEXT_ANSWER);
        updateController.setMessageView(sendMessage);
    }

    @Override
    @RabbitListener(queues = DOCUMENT_ANSWER)
    public void consume(DocumentDto documentDto) {
        log.info("received {} from node in dispatcher", DOCUMENT_ANSWER);
        updateController.setDocumentView(documentDto);
    }
}

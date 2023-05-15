package com.telegram_wb.service.impl;

import com.telegram_wb.controller.UpdateController;
import com.telegram_wb.service.AnswerConsumer;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import static com.telegram_wb.RabbitQueues.*;


@Service
@RequiredArgsConstructor
public class AnswerConsumerImpl implements AnswerConsumer {

    private final UpdateController updateController;

    @Override
    @RabbitListener(queues = TEXT_ANSWER)
    public void consume(SendMessage sendMessage) {
        updateController.setMessageView(sendMessage);
    }

    @Override
    @RabbitListener(queues = DOCUMENT_ANSWER)
    public void consume(SendDocument sendDocument) {
        updateController.setDocumentView(sendDocument);
    }
}

package com.telegram_wb.service.impl;


import com.telegram_wb.service.DocumentService;
import com.telegram_wb.service.CommandService;
import com.telegram_wb.service.UpdateConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.telegram_wb.rabbitmq.RabbitQueues.*;


@Component
@Slf4j
@RequiredArgsConstructor
public class UpdateConsumerImpl implements UpdateConsumer {

    private final DocumentService documentService;
    private final CommandService commandService;

    @Override
    @RabbitListener(queues = DOCUMENT_MESSAGE)
    public void consumeDocumentUpdate(Update update) {
        log.info("document received by the node from Rabbit");
        documentService.processDocument(update);
    }

    @Override
    @RabbitListener(queues = TEXT_MESSAGE)
    public void consumeTextUpdate(Update update) {
        log.info("text message received by the node from Rabbit");
        commandService.processText(update);
    }
}

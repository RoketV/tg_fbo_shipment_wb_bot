package com.telegram_wb.controller;

import com.telegram_wb.service.UpdateProducer;
import com.telegram_wb.utils.MessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import static com.telegram_wb.RabbitQueues.*;


@Component
@Slf4j
@RequiredArgsConstructor
public class UpdateController {

    private TelegramLongPollingBot myBot;

    private final MessageUtil messageUtil;

    private final UpdateProducer updateProducer;

    public void processUpdate(Update update) {
        if (update == null) {
            log.info("update is null");
            return;
        }
        if (update.hasMessage()) {
            distributeMessage(update);
        } else {
            log.error("Unsupported message type is received: " + update);
        }
    }

    public void setDocumentView(SendDocument sendDocument) {
        if (sendDocument == null) {
            log.info("UpdateController: document is null");
            return;
        }
        try {
            myBot.execute(sendDocument);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void setMessageView(SendMessage sendMessage) {
        if (sendMessage == null) {
            log.info("UpdateController: message is null");
            return;
        }
        try {
            myBot.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void distributeMessage(Update update) {
        if (update.getMessage().hasDocument()) {
            processDocument(update);
        }
    }

    private void sendResponse(Update update, String text) {
        SendMessage message = messageUtil.generateTextResponse(update, text);
        try {
            myBot.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void processDocument(Update update) {
        log.info("UpdateController: message is a document, started to process");
        sendResponse(update, "документ получен");
        updateProducer.produce(DOCUMENT_MESSAGE, update);
    }

    public void setMyBot(TelegramLongPollingBot myBot) {
        this.myBot = myBot;
    }
}

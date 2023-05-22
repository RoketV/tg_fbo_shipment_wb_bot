package com.telegram_wb.controller;

import com.telegram_wb.dto.DocumentDto;
import com.telegram_wb.service.UpdateProducer;
import com.telegram_wb.utils.DocumentUtil;
import com.telegram_wb.utils.MessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static com.telegram_wb.messages.AnswerConstants.*;
import static com.telegram_wb.rabbitmq.RabbitQueues.DOCUMENT_MESSAGE;
import static com.telegram_wb.rabbitmq.RabbitQueues.TEXT_MESSAGE;


@Component
@Slf4j
@RequiredArgsConstructor
public class UpdateController {

    private TelegramLongPollingBot myBot;

    private final MessageUtil messageUtil;

    private final UpdateProducer updateProducer;

    private final DocumentUtil documentUtil;

    private static final Long MAX_FILE_SIZE = 50_000_000L;

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

    public void setDocumentView(DocumentDto documentDto) {
        if (documentDto == null) {
            log.info("UpdateController: document is null");
            return;
        }
        try {
            SendDocument sendDocument = documentUtil.generateDocumentResponse(documentDto);
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
        if (update.getMessage().hasText()) {
            processText(update);
        }
    }

    private void processDocument(Update update) {
        log.info("UpdateController: message with chat id {} is a document, started to process",
                update.getMessage().getChatId());
        if (documentValidation(update)) {
            sendResponse(update, DOCUMENT_RECEIVED);
            updateProducer.produce(DOCUMENT_MESSAGE, update);
        }
    }

    private void processText(Update update) {
        log.info("UpdateController: message with chat id {} is text, started to process",
                update.getMessage().getChatId());
        updateProducer.produce(TEXT_MESSAGE, update);
    }

    private void sendResponse(Update update, String text) {
        SendMessage message = messageUtil.generateTextResponse(update, text);
        try {
            log.info("Dispatcher sent message with text {} to client", text);
            myBot.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private boolean documentValidation(Update update) {
        if (update.getMessage().getDocument().getFileSize() > MAX_FILE_SIZE) {
            log.info("document with chat id {} is more then 50 mb", update.getMessage().getChatId());
            sendResponse(update, FILE_SIZE_IS_MORE_THEN);
            return false;
        }
        if (update.getMessage().getDocument().getMimeType().contains("excel")
                || update.getMessage().getDocument().getMimeType().contains("spreadsheetml")) {
            log.info("document with chat id {} passed initial validation in dispatcher",
                    update.getMessage().getChatId());
            return true;
        }
        log.info("document with chat id {} is not an excel file", update.getMessage().getChatId());
        sendResponse(update, EXCEL_FORMAT_NEEDED);
        return false;
    }

    public void setMyBot(TelegramLongPollingBot myBot) {
        this.myBot = myBot;
    }
}

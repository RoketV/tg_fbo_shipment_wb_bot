package com.telegram_wb.service.impl;

import com.telegram_wb.dao.DocumentJpa;
import com.telegram_wb.dto.DocumentDto;
import com.telegram_wb.enums.TextCommand;
import com.telegram_wb.model.Document;
import com.telegram_wb.service.AnswerProducer;
import com.telegram_wb.util.SampleCreator;
import com.telegram_wb.service.TextService;
import com.telegram_wb.util.MessageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;

import static com.telegram_wb.messages.AnswerConstants.*;
import static com.telegram_wb.rabbitmq.RabbitQueues.DOCUMENT_ANSWER;
import static com.telegram_wb.rabbitmq.RabbitQueues.TEXT_ANSWER;

@Component
@RequiredArgsConstructor
public class TextServiceImpl implements TextService {

    private final AnswerProducer answerProducer;

    private final MessageUtil messageUtil;

    private final DocumentJpa documentJpa;

    private final SampleCreator sampleCreator;

    @Override
    public void processText(Update update) {
        String text = update.getMessage().getText();
        Optional<TextCommand> commandOptional = Optional.ofNullable(TextCommand.fromValue(text));
        if (commandOptional.isPresent()) {
            TextCommand command = commandOptional.get();
            distributeCommand(command, update);
        }
    }

    private void distributeCommand(TextCommand command, Update update) {
        switch (command) {
            case START -> processStartCommand(update);
            case HELP -> processHelpCommand(update);
            case SAMPLE_WITH_DATA -> processSampleWithDataCommand(update);
            case SKU_SAMPLE_DOCUMENT -> processSkuSampleCommand(update);
            case LAST_PROCESSED_DOCUMENT -> sendLastProcessedDocument(update);
        }
    }

    private void processStartCommand(Update update) {
        SendMessage sendMessage = messageUtil.sendMessage(update, START_MESSAGE);
        answerProducer.produce(TEXT_ANSWER, sendMessage);
    }

    private void processHelpCommand(Update update) {
        SendMessage sendMessage = messageUtil.sendMessage(update, HELP_MESSAGE);
        answerProducer.produce(TEXT_ANSWER, sendMessage);
    }

    private void processSampleWithDataCommand(Update update) {
        DocumentDto documentDto = sampleCreator.createSampleDocumentWithData();
        answerProducer.produce(DOCUMENT_ANSWER, documentDto);
    }

    private void processSkuSampleCommand(Update update) {
        DocumentDto documentDto = sampleCreator.createSampleDocumentWithSku();
        answerProducer.produce(DOCUMENT_ANSWER, documentDto);
    }


    private void sendLastProcessedDocument(Update update) {
        String chatId = update.getMessage().getChatId().toString();
        Optional<Document> document = documentJpa.getLastProcessedDocument(chatId);
        if (document.isPresent()) {
            byte[] fileBytes = document.get().getBinaryContent().getContent();
            DocumentDto documentDto = new DocumentDto(chatId, fileBytes);
            answerProducer.produce(DOCUMENT_ANSWER, documentDto);
            return;
        }
        SendMessage sendMessage = messageUtil.sendMessage(update, DOCUMENT_NOT_FOUND_IN_DB);
        answerProducer.produce(TEXT_ANSWER, sendMessage);
    }
}

package com.telegram_wb.service.impl;

import com.telegram_wb.configuration.rabbit.AnswerProducer;
import com.telegram_wb.dao.DocumentJpa;
import com.telegram_wb.dto.DocumentDto;
import com.telegram_wb.enums.TextCommand;
import com.telegram_wb.model.Document;
import com.telegram_wb.service.CommandService;
import com.telegram_wb.service.TextService;
import com.telegram_wb.util.MessageUtil;
import com.telegram_wb.util.SampleCreator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;

import static com.telegram_wb.documentNames.DocumentNames.*;
import static com.telegram_wb.messages.AnswerConstants.*;
import static com.telegram_wb.rabbitmq.RabbitQueues.DOCUMENT_ANSWER;
import static com.telegram_wb.rabbitmq.RabbitQueues.TEXT_ANSWER;

@Component
@Slf4j
@RequiredArgsConstructor
public class CommandServiceImpl implements CommandService {

    private final AnswerProducer answerProducer;

    private final MessageUtil messageUtil;

    private final DocumentJpa documentJpa;

    private final SampleCreator sampleCreator;

    private final TextService textService;

    @Override
    public void processCommand(Update update) {
        String text = update.getMessage().getText();
        Optional<TextCommand> commandOptional = Optional.ofNullable(TextCommand.fromValue(text));
        if (commandOptional.isPresent()) {
            log.info("message with chatId {} is a command {}, starting to process",
                    update.getMessage().getChatId(), commandOptional.get());
            TextCommand command = commandOptional.get();
            distributeCommand(command, update);
            return;
        }
        log.info("message is not a command");
        textService.processText(update);
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
        documentDto.setChatId(update.getMessage().getChatId().toString());
        documentDto.setName(SAMPLE_WITH_DATA_NAME);
        answerProducer.produce(DOCUMENT_ANSWER, documentDto);
    }

    private void processSkuSampleCommand(Update update) {
        DocumentDto documentDto = sampleCreator.createSampleDocumentWithSku();
        documentDto.setChatId(update.getMessage().getChatId().toString());
        documentDto.setName(SAMPLE_WITH_SKU_NAME);
        answerProducer.produce(DOCUMENT_ANSWER, documentDto);
    }

    private void sendLastProcessedDocument(Update update) {
        String chatId = update.getMessage().getChatId().toString();
        Optional<Document> document = documentJpa.getLastProcessedDocument(chatId);
        if (document.isPresent()) {
            byte[] fileBytes = document.get().getBinaryContent().getContent();
            DocumentDto documentDto = new DocumentDto(chatId, fileBytes, LAST_PROCESSED_DOCUMENT_NAME);
            answerProducer.produce(DOCUMENT_ANSWER, documentDto);
            return;
        }
        SendMessage sendMessage = messageUtil.sendMessage(update, DOCUMENT_NOT_FOUND_IN_DB);
        answerProducer.produce(TEXT_ANSWER, sendMessage);
    }
}

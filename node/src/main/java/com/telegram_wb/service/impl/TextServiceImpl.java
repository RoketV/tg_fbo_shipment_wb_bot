package com.telegram_wb.service.impl;

import com.telegram_wb.configuration.rabbit.AnswerProducer;
import com.telegram_wb.dao.DocumentJpa;
import com.telegram_wb.exceptions.InitialDocumentNotFound;
import com.telegram_wb.mapper.WorkbookMapper;
import com.telegram_wb.model.Document;
import com.telegram_wb.service.TextService;
import com.telegram_wb.util.MessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Arrays;

import static com.telegram_wb.messages.AnswerConstants.DOCUMENT_NOT_FOUND_IN_DB;
import static com.telegram_wb.messages.AnswerConstants.ERROR_COMMAND;
import static com.telegram_wb.rabbitmq.RabbitQueues.TEXT_ANSWER;

@Component
@Slf4j
@RequiredArgsConstructor
public class TextServiceImpl implements TextService {

    private final MessageUtil messageUtil;
    private final AnswerProducer answerProducer;
    private final DocumentJpa documentJpa;

    private final WorkbookMapper workbookMapper;

    @Override
    public void processText(Update update) {
        String text = update.getMessage().getText();
        if (!textIsValid(text)) {
            sendErrorCommandMessage(update);
            return;
        }
        try {
            fillDocumentWithTextData(update);
        } catch (InitialDocumentNotFound e) {
            sendNotFoundMessage(update);
        }


    }

    private void fillDocumentWithTextData(Update update) throws InitialDocumentNotFound {
        String chatId = update.getMessage().getChatId().toString();
        Document document = documentJpa.getLatestRawDocument(chatId)
                .orElseThrow(() -> new InitialDocumentNotFound("initial document with SKU was not found"));
        byte[] fileBytes = document.getBinaryContent().getContent();
        Workbook workbook = workbookMapper.createWorkbook(fileBytes);
        Sheet sheet = workbook.getSheetAt(0);

    }

    private boolean textIsValid(String text) {
        return Arrays.stream(text.split("\b")).allMatch(this::rowIsValid);
    }

    private boolean rowIsValid(String row) {
        String[] data = row.split(" ");
        return data.length == 4 && Arrays.stream(data, 1, data.length)
                .allMatch(s -> s.matches("\\d+"));
    }

    private void sendErrorCommandMessage(Update update) {
        SendMessage sendMessage = messageUtil.sendMessage(update, ERROR_COMMAND);
        answerProducer.produce(TEXT_ANSWER, sendMessage);
    }

    private void sendNotFoundMessage(Update update) {
        SendMessage sendMessage = messageUtil.sendMessage(update, DOCUMENT_NOT_FOUND_IN_DB);
        answerProducer.produce(TEXT_ANSWER, sendMessage);
    }
}

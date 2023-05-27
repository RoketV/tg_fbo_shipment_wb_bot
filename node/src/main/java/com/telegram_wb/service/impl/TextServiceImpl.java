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
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static com.telegram_wb.messages.AnswerConstants.DOCUMENT_NOT_FOUND_IN_DB;
import static com.telegram_wb.messages.AnswerConstants.ERROR_COMMAND;
import static com.telegram_wb.rabbitmq.RabbitQueues.TEXT_ANSWER;
import static com.telegram_wb.util.constants.DataDocumentConstants.DATA_CELL_BARCODE_INDEX;
import static com.telegram_wb.util.constants.DataDocumentConstants.DATA_CELL_WITH_NUMBER_OF_CARTONS;
import static com.telegram_wb.util.constants.SKUDocumentConstants.SKU_DOC_CELL_INDEX_WITH_BARCODE;
import static com.telegram_wb.util.constants.SKUDocumentConstants.SKU_DOC_CELL_INDEX_WITH_WB_SKU;

@Component
@Slf4j
@RequiredArgsConstructor
public class TextServiceImpl implements TextService {

    private final MessageUtil messageUtil;
    private final AnswerProducer answerProducer;
    private final DocumentJpa documentJpa;
    private final WorkbookMapper workbookMapper;
    private final Pattern pattern = Pattern.compile("^WB_\\d{10}$");

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
        fillWorkbookWithTextData(workbook, update);
    }

    private void fillWorkbookWithTextData(Workbook workbook, Update update) {
        String text = update.getMessage().getText();
        Sheet initialSheet = workbook.getSheetAt(0);
        String[] textRows = text.split("\n");
        CellStyle cellStyle = createDateCellStyle(workbook);
        int initialSheetRowIndex = 1;
        for (String textRow : textRows) {
            String[] data = textRow.split(" ");
            int lastRowToFillIndex = initialSheetRowIndex + Integer.parseInt(data[DATA_CELL_WITH_NUMBER_OF_CARTONS]);
            IntStream.rangeClosed(initialSheetRowIndex, lastRowToFillIndex)
                    .takeWhile(i -> initialSheet.getRow(i) != null)
                    .forEach(i -> mergeRowWithText(initialSheet.getRow(i), data, cellStyle));
        }
    }

    private void mergeRowWithText(Row initialRow, String[] data, CellStyle cellStyle) {
        Cell cellWithWBSku = initialRow.getCell(SKU_DOC_CELL_INDEX_WITH_WB_SKU);
        if (cellWithWBSku == null) {
            return;
        }
        String wbBarcode = cellWithWBSku.getStringCellValue();
        if (!wbBarcode.matches(pattern.pattern())) {
            return;
        }
        Cell cellWithBarcode = initialRow.getCell(SKU_DOC_CELL_INDEX_WITH_BARCODE,
                Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
        cellWithBarcode.setCellValue(data[DATA_CELL_BARCODE_INDEX]);

    }

    private CellStyle createDateCellStyle(Workbook workbook) {
        //TODO abstract to another class, because it is also used in WorkbookMerger
        CellStyle cellStyle = workbook.createCellStyle();
        CreationHelper creationHelper = workbook.getCreationHelper();
        cellStyle.setDataFormat(
                creationHelper.createDataFormat().getFormat("mm/dd/yyyy")
        );
        return cellStyle;
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

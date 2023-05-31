package com.telegram_wb.service.impl;

import com.telegram_wb.configuration.rabbit.AnswerProducer;
import com.telegram_wb.dao.DocumentJpa;
import com.telegram_wb.dto.DocumentDto;
import com.telegram_wb.exceptions.InitialDocumentNotFound;
import com.telegram_wb.mapper.WorkbookMapper;
import com.telegram_wb.model.BinaryContent;
import com.telegram_wb.model.Document;
import com.telegram_wb.service.TextService;
import com.telegram_wb.util.MessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static com.telegram_wb.documentNames.DocumentNames.NEW_PROCESSED_DOCUMENT_NAME;
import static com.telegram_wb.messages.AnswerConstants.*;
import static com.telegram_wb.rabbitmq.RabbitQueues.DOCUMENT_ANSWER;
import static com.telegram_wb.rabbitmq.RabbitQueues.TEXT_ANSWER;
import static com.telegram_wb.util.constants.DataDocumentConstants.*;
import static com.telegram_wb.util.constants.SKUDocumentConstants.*;
import static org.apache.poi.ss.usermodel.Row.MissingCellPolicy.CREATE_NULL_AS_BLANK;

@Component
@Slf4j
@RequiredArgsConstructor
public class TextServiceImpl implements TextService {

    private final MessageUtil messageUtil;
    private final AnswerProducer answerProducer;
    private final DocumentJpa documentJpa;
    private final WorkbookMapper workbookMapper;
    private final Pattern pattern = Pattern.compile("^WB_\\d{10}$");
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");

    @Override
    public void processText(Update update) {
        String text = update.getMessage().getText();
        try {
            if (!textIsValid(text)) {
                sendErrorCommandMessage(update);
                return;
            }
        } catch (ParseException e) {
            sendNotValidDateFormatMessage(update);
        }
        try {
            Document document = fillDocumentWithTextData(update);
            sendProcessedDocument(document);
        } catch (InitialDocumentNotFound e) {
            sendNotFoundMessage(update);
        }
    }

    private Document fillDocumentWithTextData(Update update) throws InitialDocumentNotFound {
        String chatId = update.getMessage().getChatId().toString();
        Document document = documentJpa.getLatestRawDocument(chatId)
                .orElseThrow(() -> new InitialDocumentNotFound("initial document with SKU was not found"));
        byte[] fileBytes = document.getBinaryContent().getContent();
        Workbook initialWorkbook = workbookMapper.createWorkbook(fileBytes);
        fillWorkbookWithTextData(initialWorkbook, update);
        fileBytes = workbookMapper.toFileBites(initialWorkbook);
        BinaryContent binaryContent = new BinaryContent(fileBytes);
        Document processedDocument = new Document(binaryContent, true,
                ZonedDateTime.now(ZoneId.of("Europe/Moscow")), chatId);
        documentJpa.save(processedDocument);
        return processedDocument;
    }

    private void sendProcessedDocument(Document document) {
        byte[] fileBytes = document.getBinaryContent().getContent();
        String chatId = document.getChatId();
        DocumentDto documentDto = new DocumentDto(chatId, fileBytes, NEW_PROCESSED_DOCUMENT_NAME);
        answerProducer.produce(DOCUMENT_ANSWER, documentDto);
    }

    private void fillWorkbookWithTextData(Workbook workbook, Update update) {
        String text = update.getMessage().getText();
        Sheet initialSheet = workbook.getSheetAt(0);
        String[] textRows = text.split("\n");
        CellStyle cellStyle = createDateCellStyle(workbook);
        int initialSheetRowIndex = 1;
        for (String textRow : textRows) {
            String[] data = textRow.split(" ");
            int lastRowToFillIndex = initialSheetRowIndex
                    + Integer.parseInt(data[DATA_CELL_WITH_NUMBER_OF_CARTONS]) - 1;
            IntStream.rangeClosed(initialSheetRowIndex, lastRowToFillIndex)
                    .takeWhile(i -> initialSheet.getRow(i) != null)
                    .forEach(i -> mergeRowWithText(initialSheet.getRow(i), data, cellStyle));
            initialSheetRowIndex = lastRowToFillIndex + 1;
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
        Cell cellWithBarcode = initialRow.getCell(SKU_DOC_CELL_INDEX_WITH_BARCODE, CREATE_NULL_AS_BLANK);
        cellWithBarcode.setCellValue(data[DATA_CELL_BARCODE_INDEX]);

        Cell quantityOfGoods = initialRow.getCell(SKU_DOC_CELL_INDEX_WITH_GOODS, CREATE_NULL_AS_BLANK);
        quantityOfGoods.setCellValue(data[DATA_CELL_INDEX_WITH_QUANTITY_PER_CARTON]);

        try {
            Cell date = initialRow.getCell(SKU_DOC_CELL_INDEX_WITH_EXPIRY_DATE, CREATE_NULL_AS_BLANK);
            Date expiryDate = dateFormatter.parse(data[DATA_CELL_WITH_DATA_OF_EXPIRY]);
            date.setCellValue(expiryDate);
            date.setCellStyle(cellStyle);
        } catch (ParseException e) {
            log.debug("Although row must be validated for the correct date format, " +
                    "method mergeRowWithText threw a ParseException");
        }
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

    private boolean textIsValid(String text) throws ParseException {
        String[] rows = text.split("\n");
        for (String row : rows) {
            if (!rowIsValid(row)) {
                return false;
            }
        }
        return true;
    }

    private boolean rowIsValid(String row) throws ParseException {
        String[] data = row.split(" ");
        if (data.length != 4) {
            return false;
        }
            dateFormatter.parse(data[DATA_CELL_WITH_DATA_OF_EXPIRY]);
            return data[DATA_CELL_WITH_NUMBER_OF_CARTONS].matches("\\d+")
                    && data[DATA_CELL_INDEX_WITH_QUANTITY_PER_CARTON].matches("\\d+");
    }

    private void sendErrorCommandMessage(Update update) {
        SendMessage sendMessage = messageUtil.sendMessage(update, ERROR_COMMAND);
        answerProducer.produce(TEXT_ANSWER, sendMessage);
    }

    private void sendNotFoundMessage(Update update) {
        SendMessage sendMessage = messageUtil.sendMessage(update, DOCUMENT_NOT_FOUND_IN_DB);
        answerProducer.produce(TEXT_ANSWER, sendMessage);
    }

    private void sendNotValidDateFormatMessage(Update update) {
        SendMessage sendMessage = messageUtil.sendMessage(update, NOT_VALID_DATE_FORMAT);
        answerProducer.produce(TEXT_ANSWER, sendMessage);
    }
}

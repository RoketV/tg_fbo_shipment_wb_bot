package com.telegram_wb.service.impl;

import com.telegram_wb.configuration.rabbit.AnswerProducer;
import com.telegram_wb.dao.DocumentJpa;
import com.telegram_wb.dto.DocumentDto;
import com.telegram_wb.enums.TypeOfDocument;
import com.telegram_wb.exceptions.InitialDocumentNotFound;
import com.telegram_wb.mapper.WorkbookMapper;
import com.telegram_wb.model.BinaryContent;
import com.telegram_wb.model.Document;
import com.telegram_wb.service.DocumentService;
import com.telegram_wb.util.MessageUtil;
import com.telegram_wb.util.WorkbookMerger;
import com.telegram_wb.validation.DocumentValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static com.telegram_wb.documentNames.DocumentNames.NEW_PROCESSED_DOCUMENT_NAME;
import static com.telegram_wb.messages.AnswerConstants.*;
import static com.telegram_wb.rabbitmq.RabbitQueues.DOCUMENT_ANSWER;
import static com.telegram_wb.rabbitmq.RabbitQueues.TEXT_ANSWER;

@Component
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    @Value("${service.file_info.uri}")
    private String fileInfoUri;
    @Value("${service.file_storage.uri}")
    private String fileStorageUri;
    @Value("${bot.token}")
    private String token;
    private final DocumentJpa documentJpa;
    private final DocumentValidator documentValidator;
    private final AnswerProducer answerProducer;
    private final MessageUtil messageUtilImpl;
    private final WorkbookMapper workbookMapper;
    private final WorkbookMerger workbookMerger;


    @Override
    @Transactional
    public void processDocument(Update update) {
        Message message = update.getMessage();
        String fileId = message.getDocument().getFileId();
        ResponseEntity<String> response = getFilePath(fileId);
        if (response.getStatusCode().is2xxSuccessful()) {
            URL url = getFileURLFromResponse(response);
            byte[] fileBytes = getFileByteArray(url);
            TypeOfDocument type = documentValidator.getDocumentType(fileBytes);
            switch (type) {
                case INITIAL_DOCUMENT_WITH_SKU -> processInitialDocument(fileBytes, update);
                case DOCUMENT_WITH_DATA -> processDocumentWithData(fileBytes, update);
                case NOT_VALID_DOCUMENT -> handleNotValidDocument(update);
            }
        }
    }

    private void processInitialDocument(byte[] fileBytes, Update update) {
        String chatId = update.getMessage().getChatId().toString();
        BinaryContent binaryContent = new BinaryContent(fileBytes);
        Document document = new Document(binaryContent,
                false, ZonedDateTime.now(ZoneId.of("Europe/Moscow")), chatId);
        documentJpa.save(document);
        SendMessage sendMessage = messageUtilImpl.sendMessage(update, INITIAL_DOCUMENT_WITH_SKU_SAVED);
        answerProducer.produce(TEXT_ANSWER, sendMessage);
    }

    private void processDocumentWithData(byte[] fileBytes, Update update) {
        try {
            String chatId = update.getMessage().getChatId().toString();
            Workbook workbook = parseDocumentWithData(fileBytes, chatId);
            byte[] workbookBytes = workbookMapper.toFileBites(workbook);
            DocumentDto documentDto = new DocumentDto(chatId, workbookBytes, NEW_PROCESSED_DOCUMENT_NAME);
            answerProducer.produce(DOCUMENT_ANSWER, documentDto);
            BinaryContent binaryContent = new BinaryContent(workbookBytes);
            Document document = new Document(binaryContent, true,
                    ZonedDateTime.now(ZoneId.of("Europe/Moscow")), chatId);
            documentJpa.save(document);
        } catch (InitialDocumentNotFound e) {
            handleInitialDocumentNotFound(update);
        }
    }

    private void handleNotValidDocument(Update update) {
        SendMessage sendMessage = messageUtilImpl.sendMessage(update, FILE_NOT_VALID);
        answerProducer.produce(TEXT_ANSWER, sendMessage);
    }

    private void handleInitialDocumentNotFound(Update update) {
        SendMessage sendMessage = messageUtilImpl.sendMessage(update, INITIAL_DOCUMENT_NOT_FOUND);
        answerProducer.produce(TEXT_ANSWER, sendMessage);
    }

    private Workbook parseDocumentWithData(byte[] fileBytes, String chatId) throws InitialDocumentNotFound {
        Workbook workbookWithData = workbookMapper.createWorkbook(fileBytes);
        Document initialDocument = documentJpa.getLatestRawDocument(chatId)
                .orElseThrow(() -> new InitialDocumentNotFound(String.format("Cannot process DocumentWithData, " +
                        "initial document with chatId %s not found", chatId)));
        Workbook initialWorkBook = workbookMapper.createWorkbook(initialDocument
                .getBinaryContent().getContent());
        return workbookMerger.merge(initialWorkBook, workbookWithData);
    }

    private ResponseEntity<String> getFilePath(String fileId) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);
        String fileUri = fileInfoUri.replace("{token}", token)
                .replace("{fileId}", fileId);

        return restTemplate.exchange(
                fileUri,
                HttpMethod.GET,
                request,
                String.class,
                token, fileId
        );
    }

    private URL getFileURLFromResponse(ResponseEntity<String> response) {
        JSONObject jsonObject = new JSONObject(response.getBody());
        String filePath = String.valueOf(jsonObject
                .getJSONObject("result")
                .getString("file_path"));
        try {
            return new URI(fileStorageUri.replace("{token}", token)
                    .replace("{filePath}", filePath)).toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] getFileByteArray(URL url) {
        try (InputStream is = url.openStream()) {
            return IOUtils.toByteArray(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

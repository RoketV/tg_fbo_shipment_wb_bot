package com.telegram_wb.bot.bot.service;

import com.telegram_wb.bot.document.DocumentService;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

@Service
public class BotServiceImpl implements BotService {

    @Value("${service.file_info.uri}")
    private String fileInfoUri;

    @Value("${service.file_storage.uri}")
    private String fileStorageUri;

    private TelegramLongPollingBot bot;

    private final String token = System.getenv("BOT_TOKEN");

    private final DocumentService documentService;

    public BotServiceImpl(DocumentService documentService) {
        this.documentService = documentService;
    }

    public void setBot(TelegramLongPollingBot bot) {
        this.bot = bot;
    }

    @Override
    public void sendDocument(Message message) {
        String fieldId = message.getDocument().getFileId();
        ResponseEntity<String> response = getFilePath(fieldId);
        if (response.getStatusCode().is2xxSuccessful()) {
            URL url = getFileURLFromResponse(response);
            byte[] fileBytes = getFileByteArray(url);
            XSSFWorkbook workbook = createWorkbook(fileBytes);
            documentService.processDocument(workbook);
            InputFile file = createInputFile(fileBytes);
            SendDocument document = SendDocument.builder()
                    .chatId(message.getChatId())
                    .document(file)
                    .build();
            try {
                bot.execute(document);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
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

    private XSSFWorkbook createWorkbook(byte[] fileBytes) {
        try (InputStream is = new ByteArrayInputStream(fileBytes)) {
            return new XSSFWorkbook(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private InputFile createInputFile(byte[] fileBytes) {
        try (InputStream is = new ByteArrayInputStream(fileBytes)) {
            return new InputFile(is, "name.xlsx");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

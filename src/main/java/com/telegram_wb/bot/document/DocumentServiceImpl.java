package com.telegram_wb.bot.document;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.Document;

import java.net.URL;

@Component
public class DocumentServiceImpl implements DocumentService {

    @Value("{service.file_info.uri}")
    private String fileInfoUri;

    @Value("{service.file_storage.uri}")
    private String fileStorageUri;

    private final String token = System.getenv("BOT_TOKEN");

    @Override
    public SendDocument processDocument(Document document) {
        String fieldId = document.getFileId();
        ResponseEntity<String> response = getFilePath(fieldId);
        if (response.getStatusCode().is2xxSuccessful()) {
            String fullUri = fileStorageUri.replace("{token}", token)
                    .replace("{filePath}", filePath);
            URL url =
        }
    }

    private ResponseEntity<String> getFilePath(String fileId) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);

        return restTemplate.exchange(
                fileInfoUri,
                HttpMethod.GET,
                request,
                String.class,
                token, fileId
        );
    }
}

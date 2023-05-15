package com.telegram_wb.service.impl;

import com.telegram_wb.jpa.BinaryContentJpa;
import com.telegram_wb.jpa.DocumentJpa;
import com.telegram_wb.model.BinaryContent;
import com.telegram_wb.model.Document;
import com.telegram_wb.service.DocumentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.poi.util.IOUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

@Component
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    @Value("${service.file_info.uri}")
    private String fileInfoUri;

    @Value("${service.file_storage.uri}")
    private String fileStorageUri;
    @Value("${bot.token}")
    private String token;

    private final BinaryContentJpa binaryContentJpa;

    private final DocumentJpa documentJpa;


    @Override
    @Transactional
    public void saveDocument(Update update) {
        Message message = update.getMessage();
        String fieldId = message.getDocument().getFileId();
        ResponseEntity<String> response = getFilePath(fieldId);
        if (response.getStatusCode().is2xxSuccessful()) {
            URL url = getFileURLFromResponse(response);
            String fileName = message.getDocument().getFileName();
            byte[] fileBytes = getFileByteArray(url);
            BinaryContent binaryContent = new BinaryContent(fileBytes);
            Document document = new Document(fileName,
                    binaryContent, fieldId, false);
            binaryContentJpa.save(binaryContent);
            documentJpa.save(document);
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

    private InputFile createInputFile(byte[] fileBytes) {
        try (InputStream is = new ByteArrayInputStream(fileBytes)) {
            return new InputFile(is, "name.xlsx");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

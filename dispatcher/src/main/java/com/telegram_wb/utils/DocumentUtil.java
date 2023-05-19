package com.telegram_wb.utils;

import com.telegram_wb.dto.DocumentDto;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

@Component
public class DocumentUtil {

    public SendDocument generateDocumentResponse(DocumentDto documentDto) {
        return SendDocument.builder()
                .document(Objects.requireNonNull(convertFile(documentDto.getFileBytes())))
                .chatId(documentDto.getChatId())
                .build();
    }

    private InputFile convertFile(byte[] fileBytes) {
        try (InputStream is = new ByteArrayInputStream(fileBytes)) {

            return new InputFile(is, "name.xlsx");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

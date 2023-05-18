package com.telegram_wb.util;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

@Component
public class DocumentUtil {

    public SendDocument sendDocument(Update update, Workbook workbook) {
        return SendDocument.builder()
                .chatId(update.getMessage().getChatId())
                .document(Objects.requireNonNull(convertFile(workbook)))
                .caption("Excel")
                .build();
    }

    private InputFile convertFile(Workbook workbook) {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            workbook.write(stream);
            InputStream inputStream = new ByteArrayInputStream(stream.toByteArray());
            return new InputFile(inputStream, "name.xlsx");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

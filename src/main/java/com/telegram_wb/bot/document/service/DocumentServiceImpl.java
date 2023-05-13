package com.telegram_wb.bot.document.service;

import com.telegram_wb.bot.document.dto.DocumentDto;
import com.telegram_wb.bot.document.jpa.BinaryContentJpa;
import com.telegram_wb.bot.document.jpa.DocumentJpa;
import com.telegram_wb.bot.document.mapper.DocumentMapper;
import com.telegram_wb.bot.document.model.BinaryContent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentJpa documentJpa;
    private final BinaryContentJpa binaryContentJpa;
    private final DocumentMapper documentMapper;

    @Override
    @Transactional
    public void saveDocument(DocumentDto dto) {
        BinaryContent binaryContent = dto.binaryContent();
        binaryContentJpa.save(binaryContent);
        documentJpa.save(documentMapper.fromDto(dto));
    }

    private XSSFWorkbook createWorkbook(byte[] fileBytes) {
        try (InputStream is = new ByteArrayInputStream(fileBytes)) {
            return new XSSFWorkbook(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

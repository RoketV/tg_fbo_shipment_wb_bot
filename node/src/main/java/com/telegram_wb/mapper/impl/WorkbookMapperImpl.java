package com.telegram_wb.mapper.impl;

import com.telegram_wb.dto.DocumentDto;
import com.telegram_wb.mapper.WorkbookMapper;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Component
public class WorkbookMapperImpl implements WorkbookMapper {
    @Override
    public Workbook createWorkbook(byte[] fileBytes) {
        try (InputStream is = new ByteArrayInputStream(fileBytes)) {
            return WorkbookFactory.create(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public byte[] toFileBites(Workbook workbook) {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            workbook.write(stream);
            return stream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DocumentDto toDocumentDto(Workbook workbook) {
            byte[] fileBytes = toFileBites(workbook);
            DocumentDto documentDto = new DocumentDto();
            documentDto.setFileBytes(fileBytes);
            return documentDto;
    }
}

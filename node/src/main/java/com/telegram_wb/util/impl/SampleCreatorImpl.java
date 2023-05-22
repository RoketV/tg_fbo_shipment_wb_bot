package com.telegram_wb.util.impl;

import com.telegram_wb.dto.DocumentDto;
import com.telegram_wb.util.SampleCreator;
import org.apache.poi.ss.usermodel.*;

import java.io.IOException;

public class SampleCreatorImpl implements SampleCreator {
    @Override
    public DocumentDto createSampleDocumentWithSku() {
        try {
            Workbook workbook = WorkbookFactory.create(true);
            Sheet sheet = workbook.createSheet("sheet with sku");
            Row header = sheet.createRow(0);
            fillHeadersForSampleWithSku(header);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public DocumentDto createSampleDocumentWithData() {
        return null;
    }

    private void fillHeadersForSampleWithSku(Row header) {
        String[] headers = new String[] {"баркод товара", "кол-во товаров", "шк короба", "срок годности"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            cell.setCellValue(headers[i]);
        }
    }
}

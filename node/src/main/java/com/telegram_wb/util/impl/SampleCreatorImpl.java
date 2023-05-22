package com.telegram_wb.util.impl;

import com.telegram_wb.dto.DocumentDto;
import com.telegram_wb.util.SampleCreator;
import org.apache.poi.ss.usermodel.*;

import java.io.IOException;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SampleCreatorImpl implements SampleCreator {
    
    private final static int CELL_WITH_SKU_INDEX = 2;
    private final static int QUANTITY_OF_SKU = 10;
    

    @Override
    public DocumentDto createSampleDocumentWithSku() {
        try {
            Workbook workbook = WorkbookFactory.create(true);
            Sheet sheet = workbook.createSheet("sheet with sku");
            createHeaders(sheet);
            
            fillSheetWithSku(sheet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private void fillSheetWithSku(Sheet sheet) {
        String[] skuArray = new String[QUANTITY_OF_SKU];
        for (int i = 0; i < skuArray.length; i++) {
            skuArray[i] = generateRandomSku();
        }
        for (int i = 1; i < 11; i++) {
            Row row = sheet.createRow(i);
            Cell cell = row.getCell(CELL_WITH_SKU_INDEX, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            cell.setCellValue(skuArray[i-1]);
        }
    }

    private String generateRandomSku() {
        Random random = new Random();
        String randomSku;
        Matcher matcher;
        String regex = "^WB_\\d{10}$";
        Pattern pattern = Pattern.compile(regex);
        StringBuilder sb = new StringBuilder();
        sb.append("WB_");

        for (int i = 0; i < 10; i++) {
            sb.append(random.nextInt(10));
        }

        randomSku = sb.toString();
        matcher = pattern.matcher(randomSku);

        if (!matcher.matches()) {
            return generateRandomSku();
        }

        return randomSku;
     }

    @Override
    public DocumentDto createSampleDocumentWithData() {
        return null;
    }

    private void createHeaders(Sheet sheet) {
        Row header = sheet.createRow(0);
        String[] headers = new String[] {"баркод товара", "кол-во товаров", "шк короба", "срок годности"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            cell.setCellValue(headers[i]);
        }
    }
}

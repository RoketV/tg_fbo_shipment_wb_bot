package com.telegram_wb.util.impl;

import com.telegram_wb.dto.DocumentDto;
import com.telegram_wb.mapper.WorkbookMapper;
import com.telegram_wb.util.BarcodeGenerator;
import com.telegram_wb.util.SampleCreator;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.poi.ss.usermodel.Row.MissingCellPolicy.CREATE_NULL_AS_BLANK;

@Component
@RequiredArgsConstructor
public class SampleCreatorImpl implements SampleCreator {

    private final WorkbookMapper workbookMapper;

    private final BarcodeGenerator barcodeGenerator;

    private final static int CELL_WITH_SKU_INDEX = 2;
    private final static int SAMPLE_ROWS_NUMBER = 10;


    @Override
    public DocumentDto createSampleDocumentWithSku() {
        try {
            Workbook workbook = WorkbookFactory.create(true);
            Sheet sheet = workbook.createSheet("sheet with sku");
            createHeaders(sheet);
            fillSheetWithSku(sheet);
            byte[] fileBytes = workbookMapper.toFileBites(workbook);
            DocumentDto documentDto = new DocumentDto();
            documentDto.setFileBytes(fileBytes);
            return documentDto;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DocumentDto createSampleDocumentWithData() {
        try {
            Workbook workbook = WorkbookFactory.create(true);
            Sheet sheet = workbook.createSheet("sheet with data");
            fillSheetWithData(sheet);
            
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void fillSheetWithData(Sheet sheet) {
        for (int i = 0; i < SAMPLE_ROWS_NUMBER; i++) {
            Row row = sheet.createRow(i);
            fillRowWithData(row);
        }
    }

    private void fillRowWithData(Row row) {
        String randomBarcode = barcodeGenerator.generateRandomEAN13Barcode();
        Cell cellWithBarcode = row.getCell(0, CREATE_NULL_AS_BLANK);
        cellWithBarcode.setCellValue(randomBarcode);
        Random random = new Random();
        Cell cellWithQuantityInCarton = row.getCell(1, CREATE_NULL_AS_BLANK);
        cellWithQuantityInCarton.setCellValue(random.nextInt(10, 100));
        CellStyle dataCellStryle = createDateCellStyle()
    }

    private void fillSheetWithSku(Sheet sheet) {
        String[] skuArray = new String[SAMPLE_ROWS_NUMBER];
        for (int i = 0; i < skuArray.length; i++) {
            skuArray[i] = generateRandomSku();
        }
        for (int i = 1; i < 11; i++) {
            Row row = sheet.createRow(i);
            Cell cell = row.getCell(CELL_WITH_SKU_INDEX, CREATE_NULL_AS_BLANK);
            cell.setCellValue(skuArray[i - 1]);
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
    
    private void createHeaders(Sheet sheet) {
        Row header = sheet.createRow(0);
        String[] headers = new String[]{"баркод товара", "кол-во товаров", "шк короба", "срок годности"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.getCell(i, CREATE_NULL_AS_BLANK);
            cell.setCellValue(headers[i]);
        }
    }

    private CellStyle createDateCellStyle(Workbook workbook) {
        CellStyle cellStyle = workbook.createCellStyle();
        CreationHelper creationHelper = workbook.getCreationHelper();
        cellStyle.setDataFormat(
                creationHelper.createDataFormat().getFormat("mm/dd/yyyy")
        );
        return cellStyle;
    }
}

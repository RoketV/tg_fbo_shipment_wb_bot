package com.telegram_wb.util;

import com.telegram_wb.enums.TypeOfDocument;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.regex.Pattern;

import static com.telegram_wb.enums.TypeOfDocument.INITIAL_DOCUMENT_WITH_SKU;
import static com.telegram_wb.enums.TypeOfDocument.NOT_VALID_DOCUMENT;

@Component
@Slf4j
public class DocumentValidatorImpl implements DocumentValidator {

    private final Pattern pattern = Pattern.compile("^WB_\\d{10}$");


    public TypeOfDocument getDocumentType(byte[] byteArray) {
        Workbook workbook = createWorkbook(byteArray);
        if (isNull(workbook)) {
            log.info("Validation failed: workbook or its sheet is null");
            return NOT_VALID_DOCUMENT;
        }
        Sheet sheet = workbook.getSheetAt(0);
        if (isInitialWithSku(sheet)) {
            log.info("Validation passed: workbook is {}", INITIAL_DOCUMENT_WITH_SKU);
            return INITIAL_DOCUMENT_WITH_SKU;
        }
        log.info("Validation failed: workbook does not correlate with any validation format");
        return NOT_VALID_DOCUMENT;
    }

    private Workbook createWorkbook(byte[] byteArray) {
        try (InputStream is = new ByteArrayInputStream(byteArray)) {
            return WorkbookFactory.create(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean isInitialWithSku(Sheet sheet) {
        Row firstRow = sheet.getRow(0);
        String cell0 = firstRow.getCell(0).getStringCellValue();
        String cell1 = firstRow.getCell(1).getStringCellValue();
        String cell2 = firstRow.getCell(2).getStringCellValue();
        String cell3 = firstRow.getCell(3).getStringCellValue();
        Row secondRow = sheet.getRow(1);
        String cell4 = secondRow.getCell(2).getStringCellValue();
        return Objects.equals(cell0, "баркод товара") &&
                Objects.equals(cell1, "кол-во товаров") &&
                Objects.equals(cell2, "шк короба") &&
                Objects.equals(cell3, "срок годности") &&
                cell4.matches(pattern.pattern());
    }

    private boolean isNull(Workbook workbook) {
        return workbook == null || workbook.getSheetAt(0) == null;
    }
}

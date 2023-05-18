package com.telegram_wb.validation.impl;

import com.telegram_wb.enums.TypeOfDocument;
import com.telegram_wb.util.WorkbookFabric;
import com.telegram_wb.validation.DocumentValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellAddress;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import static com.telegram_wb.enums.TypeOfDocument.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class DocumentValidatorImpl implements DocumentValidator {

    private final Pattern pattern = Pattern.compile("^WB_\\d{10}$");

    private final static Integer ACTIVE_DATA_CELLS = 4;

    private final WorkbookFabric workbookFabric;


    public TypeOfDocument getDocumentType(byte[] byteArray) {
        Workbook workbook = workbookFabric.createWorkbook(byteArray);
        if (isNull(workbook)) {
            log.info("Validation failed: workbook or its sheet is null");
            return NOT_VALID_DOCUMENT;
        }
        Sheet sheet = workbook.getSheetAt(0);
        if (isInitialWithSku(sheet)) {
            log.info("Validation passed: workbook is {}", INITIAL_DOCUMENT_WITH_SKU);
            return INITIAL_DOCUMENT_WITH_SKU;
        }
        if (isWithData(sheet)) {
            log.info("Validation passed: workbook is {}", DOCUMENT_WITH_DATA);
            return DOCUMENT_WITH_DATA;
        }
        log.info("Validation failed: workbook does not correlate with any validation format");
        return NOT_VALID_DOCUMENT;
    }

    private boolean isInitialWithSku(Sheet sheet) {
        Row firstRow = sheet.getRow(0);
        if (firstRow == null) {
            return false;
        }
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

    private boolean isWithData(Sheet sheet) {
        //TO DO work with exceptions
        CellAddress activeCellAddress = Optional.ofNullable(getFirstActiveCell(sheet))
                .orElseThrow(RuntimeException::new);
        int firstRowIndex = activeCellAddress.getRow() - 1;
        int lastRowIndex = activeCellAddress.getRow() - 1;
        int firstActiveCellWithBarcode = activeCellAddress.getColumn() - 1;

        for (int i = firstRowIndex; i < lastRowIndex; i++) {
            Row row = sheet.getRow(i);
            if (!isWithDataFormat(row, firstActiveCellWithBarcode)) {
                return false;
            }
        }
        return true;
    }

    private CellAddress getFirstActiveCell(Sheet sheet) {
        for (Row row : sheet) {
            for (Cell cell : row) {
                if (cell.getCellType() != CellType.BLANK) {
                    return cell.getAddress();
                }
            }
        }
        return null;
    }

    private boolean isWithDataFormat(Row row, int firstActiveCellIndex) {
        if (row.getPhysicalNumberOfCells() != ACTIVE_DATA_CELLS) {
            return false;
        }
        int cellWithQuantityPerCarton = firstActiveCellIndex + 1;
        int cellWithNumberOfCartons = firstActiveCellIndex + 2;
        int cellWithExpiryDate = firstActiveCellIndex + 3;
        return row.getCell(cellWithQuantityPerCarton).getCellType().equals(CellType.NUMERIC) &&
                row.getCell(cellWithNumberOfCartons).getCellType().equals(CellType.NUMERIC) &&
                DateUtil.isCellDateFormatted(row.getCell(cellWithExpiryDate));

    }

    private boolean isNull(Workbook workbook) {
        return workbook == null || workbook.getSheetAt(0) == null;
    }
}

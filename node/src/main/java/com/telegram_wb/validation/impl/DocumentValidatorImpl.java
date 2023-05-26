package com.telegram_wb.validation.impl;

import com.telegram_wb.enums.TypeOfDocument;
import com.telegram_wb.mapper.WorkbookMapper;
import com.telegram_wb.validation.DocumentValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellAddress;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

import static com.telegram_wb.enums.TypeOfDocument.*;
import static com.telegram_wb.util.constants.DataDocumentConstants.*;
import static com.telegram_wb.util.constants.SKUDocumentConstants.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class DocumentValidatorImpl implements DocumentValidator {

    private final Pattern pattern = Pattern.compile("^WB_\\d{10}$");

    private final static Integer ACTIVE_DATA_CELLS = 4;

    private final WorkbookMapper workbookMapper;


    public TypeOfDocument getDocumentType(byte[] byteArray) {
        Workbook workbook = workbookMapper.createWorkbook(byteArray);
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
        if (!allCellsAreStrings(firstRow)) {
            return false;
        }
        String cell0 = firstRow.getCell(SKU_DOC_CELL_INDEX_WITH_BARCODE).getStringCellValue();
        String cell1 = firstRow.getCell(SKU_DOC_CELL_INDEX_WITH_GOODS).getStringCellValue();
        String cell2 = firstRow.getCell(SKU_DOC_CELL_INDEX_WITH_WB_SKU).getStringCellValue();
        String cell3 = firstRow.getCell(SKU_DOC_CELL_INDEX_WITH_EXPIRY_DATE).getStringCellValue();
        Row secondRow = sheet.getRow(1);
        String cell4 = secondRow.getCell(2).getStringCellValue();
        return Objects.equals(cell0, SKU_DOC_HEADER_WITH_BARCODE) &&
                Objects.equals(cell1, SKU_DOC_HEADER_WITH_GOODS) &&
                Objects.equals(cell2, SKU_DOC_HEADER_WITH_WB_SKU) &&
                Objects.equals(cell3, SKU_DOC_HEADER_WITH_EXPIRY_DATE) &&
                cell4.matches(pattern.pattern());
    }

    private boolean allCellsAreStrings(Row row) {
        return StreamSupport.stream(row.spliterator(), false)
                .allMatch(cell -> cell.getCellType() == CellType.STRING);
    }

    private boolean isWithData(Sheet sheet) {
        //TO DO work with exceptions
        CellAddress activeCellAddress = Optional.ofNullable(getFirstActiveCell(sheet))
                .orElseThrow(RuntimeException::new);
        int firstRowIndex = activeCellAddress.getRow();
        int lastRowIndex = sheet.getLastRowNum();
        int firstActiveCellWithBarcode = activeCellAddress.getColumn();

        for (int i = firstRowIndex; i < lastRowIndex; i++) {
            Row row = sheet.getRow(i);
            if (!isWithDataFormat(row, firstActiveCellWithBarcode)) {
                return false;
            }
        }
        return true;
    }

    private CellAddress getFirstActiveCell(Sheet sheet) {
        Optional<Cell> firstActiveCell = StreamSupport.stream(sheet.spliterator(), false)
                .flatMap(row -> StreamSupport.stream(row.spliterator(), false))
                .filter(cell -> cell.getCellType() != CellType.BLANK)
                .findFirst();
        return firstActiveCell.map(Cell::getAddress).orElse(null);
    }

    private boolean isWithDataFormat(Row row, int firstActiveCellIndex) {
        if (row.getPhysicalNumberOfCells() != ACTIVE_DATA_CELLS) {
            return false;
        }
        int cellWithQuantityPerCarton = firstActiveCellIndex + DATA_CELL_INDEX_WITH_QUANTITY_PER_CARTON;
        int cellWithNumberOfCartons = firstActiveCellIndex + DATA_CELL_WITH_NUMBER_OF_CARTONS;
        int cellWithExpiryDate = firstActiveCellIndex + DATA_CELL_WITH_DATA_OF_EXPIRY;
        return row.getCell(cellWithQuantityPerCarton).getCellType().equals(CellType.NUMERIC) &&
                row.getCell(cellWithNumberOfCartons).getCellType().equals(CellType.NUMERIC) &&
                DateUtil.isCellDateFormatted(row.getCell(cellWithExpiryDate));

    }

    private boolean isNull(Workbook workbook) {
        return workbook == null || workbook.getSheetAt(0) == null;
    }
}

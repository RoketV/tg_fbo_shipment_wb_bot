package com.telegram_wb.util.impl;

import com.telegram_wb.util.WorkbookMerger;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static org.apache.poi.ss.usermodel.Row.MissingCellPolicy.CREATE_NULL_AS_BLANK;

@Component
public class WorkbookMergerImpl implements WorkbookMerger {

    private final Pattern pattern = Pattern.compile("^WB_\\d{10}$");
    private final static int CELL_WITH_ROWS_TO_FILL_INDEX = 3;

    @Override
    public Workbook merge(Workbook initial, Workbook withData) {
        int activeInitialSheetIndex = initial.getActiveSheetIndex();
        Sheet initialSheet = initial.getSheetAt(activeInitialSheetIndex);
        int activeSheetWithDataIndex = initial.getActiveSheetIndex();
        Sheet sheetWithData = withData.getSheetAt(activeSheetWithDataIndex);
        CellStyle cellStyle = createDateCellStyle(initial);
        int initialSheetRowIndex = 1;
        for (Row row : sheetWithData) {
            if (row != null) {
                int firstActiveCellIndex = findActiveCell(row);
                int rowsToFill = (int) row.getCell(firstActiveCellIndex + CELL_WITH_ROWS_TO_FILL_INDEX)
                        .getNumericCellValue() + initialSheetRowIndex;

                IntStream.rangeClosed(initialSheetRowIndex, rowsToFill)
                        .takeWhile(i -> initialSheet.getRow(i) == null)
                        .forEach(i -> mergeRows(initialSheet.getRow(i), row, cellStyle));
                initialSheetRowIndex = rowsToFill;
            }
        }
        setColumnSize(initial);
        return initial;
    }

    private void setColumnSize(Workbook initial) {
        int activeSheetIndex = initial.getActiveSheetIndex();
        Sheet sheet = initial.getSheetAt(activeSheetIndex);
        for (int i = 0; i < sheet.getRow(0).getLastCellNum(); i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void mergeRows(Row initialRow, Row rowWithData, CellStyle dateCellStyle) {
        Cell cell = initialRow.getCell(2);
        if (cell == null) {
            return;
        }
        String wbBarcode = cell.getStringCellValue();
        if (!wbBarcode.matches(pattern.pattern())) {
            return;
        }
        int firstActiveCellIndex = findActiveCell(rowWithData);
        if (firstActiveCellIndex == -1) {
            return;
        }
        long barcode = (long) rowWithData.getCell(firstActiveCellIndex).getNumericCellValue();
        initialRow.getCell(0, CREATE_NULL_AS_BLANK).setCellValue(barcode);
        int numberOfGoods = (int) rowWithData.getCell(firstActiveCellIndex + 1).getNumericCellValue();
        initialRow.getCell(1, CREATE_NULL_AS_BLANK).setCellValue(numberOfGoods);
        Date expiryDate = rowWithData.getCell(firstActiveCellIndex + 2).getDateCellValue();
        Cell date = initialRow.getCell(3, CREATE_NULL_AS_BLANK);
        date.setCellValue(expiryDate);
        date.setCellStyle(dateCellStyle);
    }

    private CellStyle createDateCellStyle(Workbook workbook) {
        CellStyle cellStyle = workbook.createCellStyle();
        CreationHelper creationHelper = workbook.getCreationHelper();
        cellStyle.setDataFormat(
                creationHelper.createDataFormat().getFormat("mm/dd/yyyy")
        );
        return cellStyle;
    }

    private int findActiveCell(Row row) {
        for (Cell cell : row) {
            if (cell != null) {
                return cell.getAddress().getColumn();
            }
        }
        return -1;
    }
}

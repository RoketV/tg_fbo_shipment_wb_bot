package com.telegram_wb.util.impl;

import com.telegram_wb.util.WorkbookMerger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.regex.Pattern;

import static org.apache.poi.ss.usermodel.Row.MissingCellPolicy.CREATE_NULL_AS_BLANK;

@Component
public class WorkbookMergerImpl implements WorkbookMerger {

    private final Pattern pattern = Pattern.compile("^WB_\\d{10}$");

    @Override
    public Workbook merge(Workbook initial, Workbook withData) {
        Sheet initialSheet = initial.getSheetAt(0);
        Sheet sheetWithData = withData.getSheetAt(0);
        int initialSheetRowIndex = 1;
        for (Row row : sheetWithData) {
            if (row != null) {
                int rowsToFill = (int) row.getCell(2).getNumericCellValue();
                for (int i = initialSheetRowIndex; i < rowsToFill; i++) {
                    if (initialSheet.getRow(i) == null) {
                        break;
                    }
                    mergeRows(initialSheet.getRow(i), row);
                }
            }
        }
        return initial;
    }

    private void mergeRows(Row initialRow, Row rowWithData) {
        Cell cell = initialRow.getCell(2);
        if (cell == null) {
            return;
        }
            String wbBarcode = cell.getStringCellValue();
        if (!wbBarcode.matches(pattern.pattern())) {
            return;
        }

        String barcode = Double.toString(rowWithData.getCell(0).getNumericCellValue());
        initialRow.getCell(0, CREATE_NULL_AS_BLANK).setCellValue(barcode);
        int numberOfGoods = (int) rowWithData.getCell(1).getNumericCellValue();
        initialRow.getCell(1, CREATE_NULL_AS_BLANK).setCellValue(numberOfGoods);
        Date expiryDate = rowWithData.getCell(3).getDateCellValue();
        initialRow.getCell(3, CREATE_NULL_AS_BLANK).setCellValue(expiryDate);
    }
}

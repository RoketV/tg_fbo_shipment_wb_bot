package com.telegram_wb.util.impl;

import com.telegram_wb.util.WorkbookMerger;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import static com.telegram_wb.util.constants.DataDocumentConstants.DATA_CELL_INDEX_WITH_QUANTITY_PER_CARTON;
import static com.telegram_wb.util.constants.DataDocumentConstants.DATA_CELL_WITH_DATA_OF_EXPIRY;
import static com.telegram_wb.util.constants.SKUDocumentConstants.*;
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
            int firstActiveCellIndex = findActiveCell(row);
            int lastRowToFillIndex = (int) row.getCell(firstActiveCellIndex + CELL_WITH_ROWS_TO_FILL_INDEX)
                    .getNumericCellValue() + initialSheetRowIndex;

            IntStream.rangeClosed(initialSheetRowIndex, lastRowToFillIndex)
                    .takeWhile(i -> initialSheet.getRow(i) != null)
                    .forEach(i -> mergeRows(initialSheet.getRow(i), row, cellStyle));
            initialSheetRowIndex = lastRowToFillIndex;
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
        //TODO properly validation
        Cell cellWithWBSku = initialRow.getCell(SKU_DOC_CELL_INDEX_WITH_WB_SKU);
        if (cellWithWBSku == null) {
            return;
        }
        String wbBarcode = cellWithWBSku.getStringCellValue();
        if (!wbBarcode.matches(pattern.pattern())) {
            return;
        }
        int firstActiveCellIndex = findActiveCell(rowWithData);
        if (firstActiveCellIndex == -1) {
            return;
        }
        Cell cellWithBarcode = rowWithData.getCell(firstActiveCellIndex);
        long barcode = cellWithBarcode.getCellType() == CellType.NUMERIC ? (long) cellWithBarcode.getNumericCellValue()
                : Long.parseLong(cellWithBarcode.getStringCellValue());
        initialRow.getCell(SKU_DOC_CELL_INDEX_WITH_BARCODE, CREATE_NULL_AS_BLANK).setCellValue(barcode);
        int numberOfGoods = (int) rowWithData.getCell(firstActiveCellIndex + DATA_CELL_INDEX_WITH_QUANTITY_PER_CARTON)
                .getNumericCellValue();
        Cell cellWithNumberOfGoods = initialRow.getCell(SKU_DOC_CELL_INDEX_WITH_GOODS, CREATE_NULL_AS_BLANK);
        cellWithNumberOfGoods.setCellValue(numberOfGoods);
        //TODO SET NUMERIC CELL style cellWithNumberOfGoods.setCellStyle();
        Date expiryDate = rowWithData.getCell(firstActiveCellIndex + DATA_CELL_WITH_DATA_OF_EXPIRY).getDateCellValue();
        Cell date = initialRow.getCell(SKU_DOC_CELL_INDEX_WITH_EXPIRY_DATE, CREATE_NULL_AS_BLANK);
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
        Optional<Cell> cellOptional = StreamSupport.stream(row.spliterator(), false)
                .filter(Objects::nonNull)
                .findFirst();

        return cellOptional.map(cell -> cell.getAddress().getColumn()).orElse(-1);
    }
}

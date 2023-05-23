package com.telegram_wb.mapper;

import org.apache.poi.ss.usermodel.Workbook;

public interface WorkbookMapper {

    Workbook createWorkbook(byte[] fileBytes);

    byte[] toFileBites(Workbook workbook);
}

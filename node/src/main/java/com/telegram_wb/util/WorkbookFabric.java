package com.telegram_wb.util;

import org.apache.poi.ss.usermodel.Workbook;

public interface WorkbookFabric {

    Workbook createWorkbook(byte[] fileBytes);
}

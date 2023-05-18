package com.telegram_wb.util;

import org.apache.poi.ss.usermodel.Workbook;

public interface WorkbookMerger {

    Workbook merge(Workbook initial, Workbook withData);
}

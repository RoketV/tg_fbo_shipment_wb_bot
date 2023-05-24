package com.telegram_wb.mapper;

import com.telegram_wb.dto.DocumentDto;
import org.apache.poi.ss.usermodel.Workbook;

public interface WorkbookMapper {

    Workbook createWorkbook(byte[] fileBytes);

    byte[] toFileBites(Workbook workbook);

    DocumentDto toDocumentDto(Workbook workbook);
}

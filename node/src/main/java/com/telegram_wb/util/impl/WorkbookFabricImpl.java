package com.telegram_wb.util.impl;

import com.telegram_wb.util.WorkbookFabric;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Component
public class WorkbookFabricImpl implements WorkbookFabric {
    @Override
    public Workbook createWorkbook(byte[] fileBytes) {
        try (InputStream is = new ByteArrayInputStream(fileBytes)) {
            return WorkbookFactory.create(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

package com.telegram_wb.bot.document;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;

@Component
public class DocumentServiceImpl implements DocumentService {



    @Override
    public SendDocument processDocument(Workbook message) {
        return null;
    }

}

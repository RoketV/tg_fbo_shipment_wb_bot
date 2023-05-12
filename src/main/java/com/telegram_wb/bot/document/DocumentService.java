package com.telegram_wb.bot.document;

import org.apache.poi.ss.usermodel.Workbook;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;

public interface DocumentService {

    SendDocument processDocument(Workbook document);
}

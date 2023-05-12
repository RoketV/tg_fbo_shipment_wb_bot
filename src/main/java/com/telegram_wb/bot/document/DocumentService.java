package com.telegram_wb.bot.document;

import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.Document;

public interface DocumentService {

    SendDocument processDocument(Document document);
}

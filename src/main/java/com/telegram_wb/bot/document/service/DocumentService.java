package com.telegram_wb.bot.document.service;

import com.telegram_wb.bot.document.dto.DocumentDto;

public interface DocumentService {

    void saveDocument(DocumentDto document);
}

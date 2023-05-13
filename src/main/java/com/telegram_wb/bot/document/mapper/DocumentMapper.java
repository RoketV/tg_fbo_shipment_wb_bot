package com.telegram_wb.bot.document.mapper;

import com.telegram_wb.bot.document.dto.DocumentDto;
import com.telegram_wb.bot.document.model.Document;

public interface DocumentMapper {

    Document fromDto(DocumentDto dto);
}

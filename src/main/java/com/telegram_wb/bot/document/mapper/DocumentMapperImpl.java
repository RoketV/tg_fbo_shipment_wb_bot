package com.telegram_wb.bot.document.mapper;

import com.telegram_wb.bot.document.dto.DocumentDto;
import com.telegram_wb.bot.document.model.Document;
import org.springframework.stereotype.Component;

@Component
public class DocumentMapperImpl implements DocumentMapper {
    @Override
    public Document fromDto(DocumentDto dto) {
        if (dto == null) {
            return null;
        }
        return new Document(dto.name(), dto.binaryContent(), dto.fileId(), dto.processed());
    }
}

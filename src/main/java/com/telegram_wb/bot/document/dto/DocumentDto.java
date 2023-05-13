package com.telegram_wb.bot.document.dto;

import com.telegram_wb.bot.document.model.BinaryContent;

public record DocumentDto(String name, BinaryContent binaryContent,
                          String fileId, Boolean processed) {
}

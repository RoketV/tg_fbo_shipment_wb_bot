package com.telegram_wb.utils;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class MessageUtil {

    public SendMessage generateTextResponse(Update update, String text) {
        if (update.getMessage() == null) {
            return null;
        }
        return SendMessage.builder()
                .chatId(update.getMessage().getChatId())
                .text(text)
                .build();
    }
}

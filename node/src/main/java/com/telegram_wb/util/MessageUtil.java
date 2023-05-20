package com.telegram_wb.util;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class MessageUtil {

    public SendMessage sendMessage(Update update, String text) {
        return SendMessage.builder()
                .chatId(update.getMessage().getChatId())
                .text(text)
                .build();
    }

    public SendMessage sendMessage(String chatId, String text) {
        return SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
    }
}

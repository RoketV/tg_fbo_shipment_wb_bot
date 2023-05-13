package com.telegram_wb.bot.bot.service;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface BotService {

    void saveDocument(Message message);

    void setBot(TelegramLongPollingBot bot);
}

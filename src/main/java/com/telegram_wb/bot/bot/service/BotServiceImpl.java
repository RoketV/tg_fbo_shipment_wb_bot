package com.telegram_wb.bot.bot.service;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class BotServiceImpl implements BotService {


    @Override
    public void sendDocument(Message message) {
        Document document = message.getDocument();
    }

}

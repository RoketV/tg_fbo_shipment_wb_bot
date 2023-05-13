package com.telegram_wb.bot.bot;

import com.telegram_wb.bot.bot.service.BotService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class MyBot extends TelegramLongPollingBot {

    private static final String USER_NAME = System.getenv("BOT_USERNAME");
    private static final String TOKEN = System.getenv("BOT_TOKEN");

    private final BotService botService;

    public MyBot(BotService botService) {
        this.botService = botService;
    }

    @PostConstruct
    public void init() {
        botService.setBot(this);
    }

    @Override
    public String getBotUsername() {
        return USER_NAME;
    }

    @Override
    public String getBotToken() {
        return TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {
        SendMessage message = SendMessage.builder()
                .chatId(update.getMessage().getChatId().toString())
                .text("Hello World")
                .build();
        botService.saveDocument(update.getMessage());
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}

package com.telegram_wb.service;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface CommandService {

    void processCommand(Update update);
}

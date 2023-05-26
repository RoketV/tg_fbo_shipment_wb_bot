package com.telegram_wb.util;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface MessageUtil {

    SendMessage sendMessage(Update update, String text);

}

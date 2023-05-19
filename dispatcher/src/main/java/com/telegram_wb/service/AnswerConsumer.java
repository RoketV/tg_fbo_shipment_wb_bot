package com.telegram_wb.service;

import com.telegram_wb.dto.DocumentDto;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public interface AnswerConsumer {

    void consume(SendMessage sendMessage);

    void consume(DocumentDto documentDto);
}

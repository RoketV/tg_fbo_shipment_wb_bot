package com.telegram_wb.service;

import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public interface AnswerProducer {

    void produce(SendMessage sendMessage);

    void produce(SendDocument sendDocument);
}

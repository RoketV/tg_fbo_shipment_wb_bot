package com.telegram_wb.service;

import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public interface AnswerProducer {

    void produce(String rabbitQueue, SendMessage sendMessage);

    void produce(String rabbitQueue, SendDocument sendDocument);
}

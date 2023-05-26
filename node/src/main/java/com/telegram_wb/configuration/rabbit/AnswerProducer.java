package com.telegram_wb.configuration.rabbit;

import com.telegram_wb.dto.DocumentDto;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public interface AnswerProducer {

    void produce(String rabbitQueue, SendMessage sendMessage);

    void produce(String rabbitQueue, DocumentDto documentDto);
}

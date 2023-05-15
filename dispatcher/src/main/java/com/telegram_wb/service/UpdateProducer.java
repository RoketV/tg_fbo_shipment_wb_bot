package com.telegram_wb.service;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface UpdateProducer {

    void produce(String RabbitQueue, Update update);
}

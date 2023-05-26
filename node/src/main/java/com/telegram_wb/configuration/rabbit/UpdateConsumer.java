package com.telegram_wb.configuration.rabbit;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface UpdateConsumer {

    void consumeDocumentUpdate(Update update);

    void consumeTextUpdate(Update update);
}

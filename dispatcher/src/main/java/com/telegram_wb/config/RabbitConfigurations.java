package com.telegram_wb.config;


import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.telegram_wb.RabbitQueues.*;

@Configuration
public class RabbitConfigurations {

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Queue docMessageQueue() {
        return new Queue(DOCUMENT_MESSAGE);
    }

    @Bean
    public Queue textMessageQueue() {
        return new Queue(TEXT_MESSAGE);
    }

    @Bean
    public Queue docAnswerQueue() {
        return new Queue(DOCUMENT_ANSWER);
    }

    @Bean
    public Queue textAnswerQueue() {
        return new Queue(TEXT_ANSWER);
    }
}

package com.telegram_wb.util;

import com.telegram_wb.dao.DocumentJpa;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Component
@RequiredArgsConstructor
public class DatabaseCleaner {

    private final static String TIME_ZONE = "Europe/Moscow";

    private final DocumentJpa documentJpa;

    @Scheduled(cron = "0 0 2 * * ?", zone = TIME_ZONE)
    public void cleanupDatabase() {
        ZonedDateTime time = ZonedDateTime.now(ZoneId.of(TIME_ZONE)).plusDays(2);
        documentJpa.clearDatabaseByTime(time);
    }
}

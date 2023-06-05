package com.telegram_wb.dao;


import com.telegram_wb.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.ZonedDateTime;
import java.util.Optional;

public interface DocumentJpa extends JpaRepository<Document, Long> {

    @Query("from Document d where d.chatId=?1 and d.processed=false and d.timestamp = " +
            "(select max(d2.timestamp) from Document d2 where d2.chatId = ?1)")
    Optional<Document> getLatestRawDocument(String chatId);

    @Query("from Document d where d.chatId=?1 and d.processed=true and d.timestamp = " +
            "(select max(d2.timestamp) from Document d2 where d2.chatId = ?1)")
    Optional<Document> getLastProcessedDocument(String chatId);

    @Query("delete from Document d where d.timestamp > ?1")
    void clearDatabaseByTime(ZonedDateTime time); //TODO time delete


}

package com.telegram_wb.dao;


import com.telegram_wb.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface DocumentJpa extends JpaRepository<Document, Long> {

    @Query("from Document d where d.chatId=?1 order by d.timestamp desc ")
    Optional<Document> getLatestRawDocument(String chatId);

}

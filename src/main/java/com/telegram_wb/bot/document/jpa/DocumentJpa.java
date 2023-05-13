package com.telegram_wb.bot.document.jpa;

import com.telegram_wb.bot.document.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentJpa extends JpaRepository<Document, Long> {
}

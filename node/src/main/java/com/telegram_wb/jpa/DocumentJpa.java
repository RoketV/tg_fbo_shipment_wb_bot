package com.telegram_wb.jpa;


import com.telegram_wb.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentJpa extends JpaRepository<Document, Long> {
}

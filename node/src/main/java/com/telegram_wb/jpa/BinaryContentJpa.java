package com.telegram_wb.jpa;


import com.telegram_wb.model.BinaryContent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BinaryContentJpa extends JpaRepository<BinaryContent, Long> {
}

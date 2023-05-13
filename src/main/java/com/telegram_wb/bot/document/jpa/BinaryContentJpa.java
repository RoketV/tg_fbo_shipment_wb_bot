package com.telegram_wb.bot.document.jpa;

import com.telegram_wb.bot.document.model.BinaryContent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BinaryContentJpa extends JpaRepository<BinaryContent, Long> {
}

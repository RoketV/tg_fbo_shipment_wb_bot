package com.telegram_wb.bot.document.model;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "binary_content")
public class BinaryContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "content")
    private byte[] content;

    public BinaryContent() {
    }

    public BinaryContent(byte[] content) {
        this.content = content;
    }
}

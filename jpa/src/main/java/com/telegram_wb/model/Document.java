package com.telegram_wb.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "document")
public class Document {

    public Document() {
    }

    public Document(BinaryContent binaryContent, String fileId, Boolean processed,
                    LocalDateTime timestamp, String chatId) {
        this.binaryContent = binaryContent;
        this.fileId = fileId;
        this.processed = processed;
        this.timestamp = timestamp;
        this.chatId = chatId;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private BinaryContent binaryContent;

    @Column(name = "telegram_file_id")
    private String fileId;

    @Column(name = "processed")
    private Boolean processed;

    private LocalDateTime timestamp;

    private String chatId;
}


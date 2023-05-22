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

    public Document(BinaryContent binaryContent, Boolean processed,
                    LocalDateTime timestamp, String chatId) {
        this.binaryContent = binaryContent;
        this.processed = processed;
        this.timestamp = timestamp;
        this.chatId = chatId;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    private BinaryContent binaryContent;

    @Column(name = "processed")
    private Boolean processed;

    private LocalDateTime timestamp;

    private String chatId;
}


package com.telegram_wb.bot.document.model;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "document")
public class Document {

    public Document() {
    }

    public Document(String name, BinaryContent binaryContent, String fileId, Boolean processed) {
        this.name = name;
        this.binaryContent = binaryContent;
        this.fileId = fileId;
        this.processed = processed;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @OneToOne
    private BinaryContent binaryContent;

    @Column(name = "telegram_file_id")
    private String fileId;

    @Column(name = "processed")
    private Boolean processed;
}

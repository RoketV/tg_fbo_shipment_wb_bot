package com.telegram_wb.dto;


public class DocumentDto {

    private String chatId;
    private byte[] fileBytes;

    public DocumentDto() {
    }

    public DocumentDto(String chatId, byte[] fileBytes) {
        this.chatId = chatId;
        this.fileBytes = fileBytes;
    }

    public String getChatId() {
        return chatId;
    }

    public byte[] getFileBytes() {
        return fileBytes;
    }


}

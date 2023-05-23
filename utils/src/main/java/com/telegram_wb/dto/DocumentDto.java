package com.telegram_wb.dto;

import lombok.Getter;
import lombok.Setter;


import java.util.Arrays;
import java.util.Objects;


@Setter
@Getter
public class DocumentDto {

    private String chatId;
    private byte[] fileBytes;

    private String name;

    public DocumentDto() {
    }

    public DocumentDto(String chatId, byte[] fileBytes, String name) {
        this.chatId = chatId;
        this.fileBytes = fileBytes;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DocumentDto that)) return false;
        return Objects.equals(getChatId(), that.getChatId()) && Arrays.equals(getFileBytes(), that.getFileBytes()) && Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(getChatId(), getName());
        result = 31 * result + Arrays.hashCode(getFileBytes());
        return result;
    }
}

package com.telegram_wb.enums;

public enum TextCommand {

    START("/start"),
    HELP("/help"),
    SKU_SAMPLE_DOCUMENT("/sample_with_sku"),
    SAMPLE_WITH_DATA("/sample_with_data"),
    LAST_PROCESSED_DOCUMENT("/last_processed_documents");

    private final String value;

    TextCommand(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static TextCommand fromValue(String value) {
        for (TextCommand textCommand : TextCommand.values()) {
            if (textCommand.value.equals(value)) {
                return textCommand;
            }
        }
        return null;
    }
}

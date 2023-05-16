package com.telegram_wb.util;

import com.telegram_wb.enums.TypeOfDocument;

public interface DocumentValidator {

    TypeOfDocument getDocumentType(byte[] byteArray);
}

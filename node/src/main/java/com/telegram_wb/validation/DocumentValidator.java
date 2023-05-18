package com.telegram_wb.validation;

import com.telegram_wb.enums.TypeOfDocument;

public interface DocumentValidator {

    TypeOfDocument getDocumentType(byte[] byteArray);
}

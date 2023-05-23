package com.telegram_wb.util.impl;

import com.telegram_wb.util.BarcodeGenerator;
import org.springframework.stereotype.Component;

@Component
public class BarcodeGeneratorImpl implements BarcodeGenerator {
    @Override
    public String generateRandomEAN13Barcode() {
        StringBuilder sb = new StringBuilder();
        sb.append("2"); // Start with "2" for EAN-13 format

        for (int i = 0; i < 11; i++) {
            int randomNumber = (int) (Math.random() * 10);
            sb.append(randomNumber);
        }

        String barcodeData = sb.toString();
        int checkDigit = calculateEAN13CheckDigit(barcodeData);
        sb.append(checkDigit);

        return sb.toString();
    }

    private static int calculateEAN13CheckDigit(String barcodeData) {
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = Character.getNumericValue(barcodeData.charAt(i));
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        return (10 - (sum % 10)) % 10;
    }
}

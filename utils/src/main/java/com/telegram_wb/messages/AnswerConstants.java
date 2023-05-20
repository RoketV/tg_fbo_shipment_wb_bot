package com.telegram_wb.messages;

public class AnswerConstants {

    public final static String INITIAL_DOCUMENT_NOT_FOUND = """
            Сначала нужно загрузить документ со штрихкодами.
            
            Его можно найти так:
            
            Поставки ➡ Нужная поставка ➡ Упаковка ➡ Скачать XLS\040
            ➡ Скачать ШХ Коробов.""";
    public final static String DOCUMENT_RECEIVED = "Документ получен и обрабатывается";
    public final static String FILE_SIZE_IS_MORE_THEN = "Файл не может быть больше 50 мб";
    public final static String EXCEL_FORMAT_NEEDED = "Файл должен быть в формате Excel";
}

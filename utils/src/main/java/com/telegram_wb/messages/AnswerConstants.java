package com.telegram_wb.messages;

public class AnswerConstants {

    public final static String INITIAL_DOCUMENT_NOT_FOUND = """
            Сначала нужно загрузить документ со штрихкодами.
                        
            Его можно найти так:
                        
            Поставки ➡ Нужная поставка ➡ Упаковка ➡ Скачать XLS
            ➡ Скачать ШХ Коробов.""";
    public final static String DOCUMENT_RECEIVED = "Я получил документ и взялся за дело!\uD83E\uDD16";
    public final static String INITIAL_DOCUMENT_WITH_SKU_SAVED = """
            Документ со штрихкодами и сохранён в системе✅
                        
            Теперь пришлите документ с данными о поставке
            ℹ
            Выберите /sample_with_data, чтобы посмотреть на образец
                        
            Либо пришлите данные о поставке текстом в формате:
                        
            123456789 65 28.01.2025 10
                        
            Объясню:
                        
            123456789 — это ваш баркод
            65 — это количество товаров в одном коробе
            28.01.2025 — срок годности
            10 — количество коробов
                        
            В Excel файле распологайте информацию в ячейках в такой же последовательности""";
    public final static String FILE_SIZE_IS_MORE_THEN = "Файл не может быть больше 50 мб";
    public final static String EXCEL_FORMAT_NEEDED = "Файл должен быть в формате Excel";
    public final static String FILE_NOT_VALID = """
            Файл не прошёл валидацию.
                        
            Нажмите /sample_with_sku, чтобы получить образец документа со штрихкодами от ВБ
            Либо нажмите /sample_with_data, чтобы посмотреть как шаблон с данными о поставке
            """;
    public final static String HELP_MESSAGE = """
                        
            """;
    public final static String START_MESSAGE = """
                        
            """;
    public final static String DOCUMENT_NOT_FOUND_IN_DB = """
            Не смог найти подходящий файл.
            Может быть он хранился у меня больше недели и я его потерял.
            Либо вы мне ещё не присылали документы на обработку.
                        
            Если всё-таки присылали, то напишите моему создателю
            @vladyslav_moroz_96. Он подкрутит.
            """;

    public final static String ERROR_COMMAND = """
            Прошу прощения, но я не могу понять, что здесь написано.
            
            Пожалуйста, используйте меню с командами или пришлите мне файл.
             """;
}

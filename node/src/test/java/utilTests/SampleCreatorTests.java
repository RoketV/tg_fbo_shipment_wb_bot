package utilTests;


import com.telegram_wb.mapper.impl.WorkbookMapperImpl;
import com.telegram_wb.util.impl.BarcodeGeneratorImpl;
import com.telegram_wb.util.impl.SampleCreatorImpl;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class SampleCreatorTests {



    @Test
    public void GenerateRandomSku_ShouldGenerate() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String regex = "^WB_\\d{10}$";
        Pattern pattern = Pattern.compile(regex);
        Method method = SampleCreatorImpl.class.getDeclaredMethod("generateRandomSku");
        method.setAccessible(true);
        SampleCreatorImpl sampleCreator = new SampleCreatorImpl(new WorkbookMapperImpl(), new BarcodeGeneratorImpl());

        String randomSku = (String) method.invoke(sampleCreator);

        Matcher matcher = pattern.matcher(randomSku);
        assertTrue(matcher.matches());
    }

    @Test
    public void createHeaders_ShouldCreateFourHeaders() throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Workbook workbook = WorkbookFactory.create(true);
        Sheet sheet = workbook.createSheet();

        Method createHeadersMethod = SampleCreatorImpl.class.getDeclaredMethod("createHeaders", Sheet.class);
        createHeadersMethod.setAccessible(true);
        SampleCreatorImpl sampleCreator = new SampleCreatorImpl(new WorkbookMapperImpl(), new BarcodeGeneratorImpl());

        createHeadersMethod.invoke(sampleCreator, sheet);
        Row row = sheet.getRow(0);
        assertAll(
                () -> assertEquals("баркод товара", row.getCell(0).getStringCellValue()),
                () -> assertEquals("кол-во товаров", row.getCell(1).getStringCellValue()),
                () -> assertEquals("шк короба", row.getCell(2).getStringCellValue()),
                () -> assertEquals("срок годности", row.getCell(3).getStringCellValue()
                ));
    }

    @Test
    public void fillSheetWithSku_ShouldCreate10RowsStartingFromSecondOne() throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Workbook workbook = WorkbookFactory.create(true);
        Sheet sheet = workbook.createSheet();
        int lastRowIndex = 10;

        Method fillSheetWithSkuMethod = SampleCreatorImpl.class.getDeclaredMethod("fillSheetWithSku", Sheet.class);
        fillSheetWithSkuMethod.setAccessible(true);
        SampleCreatorImpl sampleCreator = new SampleCreatorImpl(new WorkbookMapperImpl(), new BarcodeGeneratorImpl());

        fillSheetWithSkuMethod.invoke(sampleCreator, sheet);
        int lastRowIndexAfterMethod = sheet.getLastRowNum();
        assertEquals(lastRowIndex, lastRowIndexAfterMethod);
    }
}

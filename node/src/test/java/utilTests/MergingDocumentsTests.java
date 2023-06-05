package utilTests;

import com.telegram_wb.NodeApplication;
import com.telegram_wb.dto.DocumentDto;
import com.telegram_wb.mapper.WorkbookMapper;
import com.telegram_wb.util.SampleCreator;
import com.telegram_wb.util.WorkbookMerger;
import com.telegram_wb.validation.DocumentValidator;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import static com.telegram_wb.enums.TypeOfDocument.DOCUMENT_WITH_DATA;
import static com.telegram_wb.enums.TypeOfDocument.INITIAL_DOCUMENT_WITH_SKU;
import static com.telegram_wb.util.constants.SKUDocumentConstants.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = NodeApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(locations = "classpath:application-test.properties")
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MergingDocumentsTests {

    private final SampleCreator sampleCreator;
    private final DocumentValidator documentValidator;
    private final WorkbookMerger workbookMerger;
    private final WorkbookMapper workbookMapper;

    @BeforeAll
    @Sql("classpath:schema.sql")
    public static void testConfig() {

    }

    @Test
    public void createSamples_shouldBeRightTypes() {
        DocumentDto skuDto = sampleCreator.createSampleDocumentWithSku();
        DocumentDto sampleDto = sampleCreator.createSampleDocumentWithData();

        assertAll(
                () -> assertTrue(sampleDto.getFileBytes().length != 0),
                () -> assertTrue(skuDto.getFileBytes().length != 0),
                () -> assertEquals(DOCUMENT_WITH_DATA, documentValidator.getDocumentType(sampleDto.getFileBytes())),
                () -> assertEquals(INITIAL_DOCUMENT_WITH_SKU, documentValidator.getDocumentType(skuDto.getFileBytes()))
        );
    }

    @Test
    public void createInitialDocument_AllCellsExceptWBBarcodeShouldBeNull() {
        DocumentDto skuDto = sampleCreator.createSampleDocumentWithSku();
        Workbook initialWorkbook = workbookMapper.createWorkbook(skuDto.getFileBytes());

        Sheet initialSheet = initialWorkbook.getSheetAt(0);
        for (int i = 1; i < initialSheet.getLastRowNum(); i++) {
            Row row = initialSheet.getRow(i);
            Cell cellWithBarcode = row.getCell(SKU_DOC_CELL_INDEX_WITH_BARCODE);
            Cell cellWithNumberOfGoods = row.getCell(SKU_DOC_CELL_INDEX_WITH_GOODS);
            Cell cellWithExpiryDate = row.getCell(SKU_DOC_CELL_INDEX_WITH_EXPIRY_DATE);
            Cell cellWithSKU = row.getCell(SKU_DOC_CELL_INDEX_WITH_WB_SKU);

            assertAll(
                    () -> assertNull(cellWithBarcode),
                    () -> assertNull(cellWithNumberOfGoods),
                    () -> assertNull(cellWithExpiryDate),
                    () -> assertTrue(cellWithSKU.getStringCellValue().matches("^WB_\\d{10}$"))
            );
        }
    }

    @Test
    public void createSamples_shouldMergeSuccessfully() {
        DocumentDto skuDto = sampleCreator.createSampleDocumentWithSku();
        DocumentDto sampleDto = sampleCreator.createSampleDocumentWithData();

        Workbook initialWorkbook = workbookMapper.createWorkbook(skuDto.getFileBytes());
        Workbook workbookWithData = workbookMapper.createWorkbook(sampleDto.getFileBytes());

        Workbook parsedDocument = workbookMerger.merge(initialWorkbook, workbookWithData);

        Row workbookHeader = parsedDocument.getSheetAt(0).getRow(0);
        Row initialSkuDocumentHeader = initialWorkbook.getSheetAt(0).getRow(0);

        assertEquals(initialSkuDocumentHeader, workbookHeader);

        Sheet parsedSheet = initialWorkbook.getSheetAt(0);
        for (int i = 1; i < parsedSheet.getLastRowNum(); i++) {
            Row row = parsedSheet.getRow(i);
            Cell cellWithBarcode = row.getCell(SKU_DOC_CELL_INDEX_WITH_BARCODE);
            Cell cellWithNumberOfGoods = row.getCell(SKU_DOC_CELL_INDEX_WITH_GOODS);
            Cell cellWithExpiryDate = row.getCell(SKU_DOC_CELL_INDEX_WITH_EXPIRY_DATE);

            assertAll(
                    () -> assertNotNull(cellWithBarcode),
                    () -> assertEquals(CellType.NUMERIC, cellWithBarcode.getCellType()),
                    () -> assertNotNull(cellWithNumberOfGoods),
                    () -> assertEquals(CellType.NUMERIC, cellWithNumberOfGoods.getCellType()),
                    () -> assertNotNull(cellWithExpiryDate)
            );
        }
    }
}


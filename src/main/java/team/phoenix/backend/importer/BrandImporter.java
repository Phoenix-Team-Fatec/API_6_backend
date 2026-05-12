package team.phoenix.backend.importer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import team.phoenix.backend.domain.model.Brand;
import team.phoenix.backend.domain.repository.BrandRepository;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class BrandImporter {

    private static final int COD_MARCA_COLUMN = 1;
    private static final int DESCRI_MARCA_COLUMN = 2;

    private final BrandRepository brandRepository;

    public int importFromBaseRh() throws IOException {
        Map<Integer, Brand> brandsByCodigo = new LinkedHashMap<>();

        for (String path : DataImportService.RH_FILES) {
            readBrands(path, brandsByCodigo);
        }

        var newBrands = brandsByCodigo.values().stream()
            .filter(brand -> brandRepository.findByCodigo(brand.getCodigo()).isEmpty())
            .toList();

        brandRepository.saveAll(newBrands);
        return newBrands.size();
    }

    private void readBrands(String path, Map<Integer, Brand> brandsByCodigo) throws IOException {
        try (Workbook wb = new XSSFWorkbook(new FileInputStream(path))) {
            Sheet sheet = wb.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Integer codigo = parseInt(row.getCell(COD_MARCA_COLUMN));
                String descricao = parseString(row.getCell(DESCRI_MARCA_COLUMN));
                if (codigo == null || descricao == null || descricao.isBlank()) {
                    log.debug("Ignoring brand row {} from {} because codigo or descricao is empty", i, path);
                    continue;
                }

                brandsByCodigo.putIfAbsent(codigo, Brand.builder()
                    .codigo(codigo)
                    .nome(descricao.trim())
                    .descricao(descricao.trim())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(null)
                    .build());
            }
        }
    }

    private Integer parseInt(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case NUMERIC -> (int) cell.getNumericCellValue();
            case STRING -> parseIntegerString(cell.getStringCellValue());
            default -> null;
        };
    }

    private Integer parseIntegerString(String value) {
        if (value == null || value.isBlank()) return null;
        return Integer.valueOf(value.trim());
    }

    private String parseString(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            case BLANK -> null;
            default -> cell.toString().trim();
        };
    }
}

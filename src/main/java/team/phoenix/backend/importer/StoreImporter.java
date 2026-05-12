package team.phoenix.backend.importer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import team.phoenix.backend.domain.model.Store;
import team.phoenix.backend.domain.repository.StoreRepository;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class StoreImporter {

    private static final int COD_LOJA_COLUMN = 3;
    private static final int DESCR_LOJA_COLUMN = 4;

    private final StoreRepository storeRepository;

    public int importFromBaseRh() throws IOException {
        Map<Integer, Store> storesByCodigo = new LinkedHashMap<>();

        for (String path : DataImportService.RH_FILES) {
            readStores(path, storesByCodigo);
        }

        var newStores = storesByCodigo.values().stream()
            .filter(store -> storeRepository.findByCodigo(store.getCodigo()).isEmpty())
            .toList();

        storeRepository.saveAll(newStores);
        return newStores.size();
    }

    private void readStores(String path, Map<Integer, Store> storesByCodigo) throws IOException {
        try (Workbook wb = new XSSFWorkbook(new FileInputStream(path))) {
            Sheet sheet = wb.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Integer codigo = parseInt(row.getCell(COD_LOJA_COLUMN));
                String descricao = parseString(row.getCell(DESCR_LOJA_COLUMN));
                if (codigo == null || descricao == null || descricao.isBlank()) {
                    log.debug("Ignoring store row {} from {} because codigo or descricao is empty", i, path);
                    continue;
                }

                storesByCodigo.putIfAbsent(codigo, Store.builder()
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

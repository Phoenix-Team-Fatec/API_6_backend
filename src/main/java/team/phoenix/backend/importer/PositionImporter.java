package team.phoenix.backend.importer;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import team.phoenix.backend.domain.model.Position;
import team.phoenix.backend.domain.repository.PositionRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class PositionImporter {

    private static final String COD_CARGO_HEADER = "Cod_Cargo";
    private static final String DESCRI_CARGO_HEADER = "Descri_Cargo";

    private final PositionRepository positionRepository;

    public int importFromCommissionBase() throws IOException {
        return importFromFile(DataImportService.COMMISSION_FILE);
    }

    int importFromFile(String path) throws IOException {
        Map<Integer, Position> positionsByCodigo = new LinkedHashMap<>();
        readPositions(path, positionsByCodigo);

        var newPositions = positionsByCodigo.values().stream()
            .filter(position -> positionRepository.findByCodigo(position.getCodigo()).isEmpty())
            .toList();

        positionRepository.saveAll(newPositions);
        return newPositions.size();
    }

    private void readPositions(String path, Map<Integer, Position> positionsByCodigo) throws IOException {
        try (Workbook wb = new XSSFWorkbook(new FileInputStream(path))) {
            Sheet sheet = wb.getSheetAt(0);
            Map<String, Integer> headers = readHeaders(sheet.getRow(0));
            Integer codigoColumn = headers.get(normalizeHeader(COD_CARGO_HEADER));
            Integer descricaoColumn = headers.get(normalizeHeader(DESCRI_CARGO_HEADER));

            if (codigoColumn == null || descricaoColumn == null) {
                throw new IllegalStateException("Colunas obrigatórias não encontradas em " + path
                    + ": " + COD_CARGO_HEADER + ", " + DESCRI_CARGO_HEADER);
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Integer codigo = parseInt(row.getCell(codigoColumn));
                String descricao = parseString(row.getCell(descricaoColumn));
                if (codigo == null || descricao == null || descricao.isBlank()) {
                    log.debug("Ignoring position row {} from {} because codigo or descricao is empty", i, path);
                    continue;
                }

                String trimmedDescricao = descricao.trim();
                positionsByCodigo.putIfAbsent(codigo, Position.builder()
                    .codigo(codigo)
                    .nome(trimmedDescricao)
                    .descricao(trimmedDescricao)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(null)
                    .build());
            }
        }
    }

    private Map<String, Integer> readHeaders(Row headerRow) {
        Map<String, Integer> headers = new LinkedHashMap<>();
        if (headerRow == null) {
            return headers;
        }

        for (Cell cell : headerRow) {
            String header = parseString(cell);
            if (header != null && !header.isBlank()) {
                headers.put(normalizeHeader(header), cell.getColumnIndex());
            }
        }
        return headers;
    }

    private String normalizeHeader(String header) {
        return header.trim().toLowerCase(Locale.ROOT);
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
